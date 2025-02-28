package toph.repository

import com.arangodb.model.StreamTransactionOptions
import com.softwaremill.macwire.wire
import one.estrondo.farango.zio.given
import toph.model.objectstorage.FolderPath
import toph.model.objectstorage.ObjectContent
import toph.model.objectstorage.ObjectItem
import toph.model.objectstorage.ObjectPath
import toph.repository.objectstorage.StoredObject
import toph.repository.objectstorage.UpdateRepository
import tremors.zio.farango.CollectionManager
import zio.Task
import zio.ZIO

trait ObjectStorageRepository:

  def load(objectPath: ObjectPath): Task[Option[ObjectContent]]

  def load(folderPath: FolderPath): Task[Seq[ObjectItem]]

  def transaction[A](f: UpdateRepository => Task[A]): Task[A]

object ObjectStorageRepository:

  private val aqlLoadObjectContent =
    """
      |FOR o IN @@coll
      | FILTER o.path == @path
      | FILTER o.owner == @owner
      | RETURN o""".stripMargin

  private val aqlLocalFolder =
    """
      |FOR o IN @@coll
      | FILTER o.folder == @folder
      | FILTER o.owner == @owner
      | RETURN {key:o._key,folder:o.folder,name:o.name,contentType:o.contentType,createdAt:o.createdAt,updatedAt:o.updatedAt}""".stripMargin

  def apply(collectionManager: CollectionManager): ObjectStorageRepository =
    wire[Impl]

  class Impl(collectionManager: CollectionManager) extends ObjectStorageRepository:

    override def load(objectPath: ObjectPath): Task[Option[ObjectContent]] =
      database
        .query[StoredObject, ObjectContent](
          aqlLoadObjectContent,
          Map(
            "@coll" -> collection.name,
            "path"  -> objectPath.canonicalPath,
            "owner" -> objectPath.owner,
          ),
        )
        .runHead

    override def load(folderPath: FolderPath): Task[Seq[ObjectItem]] =
      database
        .query[ObjectItem, ObjectItem](
          query = aqlLocalFolder,
          bindVars = Map(
            "@coll"  -> collection.name,
            "folder" -> folderPath.name,
            "owner"  -> folderPath.owner,
          ),
        )
        .runCollect

    private def database = collectionManager.database

    private def collection = collectionManager.collection

    override def transaction[A](f: UpdateRepository => Task[A]): Task[A] =
      ZIO
        .attemptBlocking(
          arangoDatabase.beginStreamTransaction(StreamTransactionOptions().writeCollections(collection.name)),
        )
        .flatMap { transactionEntity =>
          f(UpdateRepository(collectionManager, transactionEntity)).foldCauseZIO(
            success = { result =>
              ZIO
                .attemptBlocking(arangoDatabase.commitStreamTransaction(transactionEntity.getId))
                .ignoreLogged as result
            },
            failure = { cause =>
              ZIO
                .attemptBlocking(arangoDatabase.abortStreamTransaction(transactionEntity.getId))
                .ignoreLogged *> ZIO.failCause(cause)
            },
          )
        }

    private def arangoDatabase = collectionManager.database.arango
