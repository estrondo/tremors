package toph.repository.objectstorage

import com.arangodb.entity.StreamTransactionEntity
import com.arangodb.model.AqlQueryOptions
import com.arangodb.model.DocumentCreateOptions
import com.arangodb.model.DocumentUpdateOptions
import com.softwaremill.macwire.wire
import io.github.arainko.ducktape.Field
import java.time.ZonedDateTime
import one.estrondo.farango.FarangoTransformer
import one.estrondo.farango.ducktape.*
import one.estrondo.farango.zio.given
import toph.model.objectstorage.FolderPath
import toph.model.objectstorage.ObjectContent
import toph.model.objectstorage.ObjectItem
import toph.model.objectstorage.ObjectPath
import tremors.zio.farango.CollectionManager
import zio.Task

trait UpdateRepository:

  def create(action: CreateObject): Task[CreateObject]

  def load(objectPath: ObjectPath): Task[Option[ObjectContent]]

  def load(folderPath: FolderPath): Task[Seq[ObjectItem]]

  def removeFolder(folderPath: FolderPath): Task[Unit]

  def removeObject(key: String): Task[Unit]

  def search(objectPath: ObjectPath): Task[Option[ObjectItem]]

  def update(action: UpdateObject): Task[UpdateObject]

object UpdateRepository:

  private val aqlLoadObject =
    """
      |FOR o IN @@coll
      | FILTER o.path == @path
      | FILTER o.owner == @owner
      | RETURN o""".stripMargin

  private val aqlSearchObjectItem =
    """
      |FOR o IN @@coll
      | FILTER o.path == @path
      | FILTER o.owner == @owner
      | RETURN {key:o._key,folder:o.folder,name:o.name,contentType:o.contentType,createdAt:o.createdAt,updatedAt:o.updatedAt}""".stripMargin

  private val aqlLoadFolder =
    """
      |FOR o IN @@coll
      | FILTER o.path LIKE @path
      | FILTER o.owner == @owner
      | RETURN {key:o._key,folder:o.folder,name:o.name,contentType:o.contentType,createdAt:o.createdAt,updatedAt:o.updatedAt}""".stripMargin

  def apply(collectionManager: CollectionManager, transactionEntity: StreamTransactionEntity): UpdateRepository =
    wire[Impl]

  private given FarangoTransformer[UpdateObject, U] = DucktapeTransformer(
    Field.renamed(_.updatedAt, _.now),
  )

  class Impl(collectionManager: CollectionManager, transactionEntity: StreamTransactionEntity) extends UpdateRepository:

    override def create(action: CreateObject): Task[CreateObject] =
      collection
        .insertDocument[StoredObject, K](
          document = action,
          options = DocumentCreateOptions().streamTransactionId(transactionEntity.getId),
        ) as action

    override def load(objectPath: ObjectPath): Task[Option[ObjectContent]] =
      database
        .query[StoredObject, ObjectContent](
          query = aqlLoadObject,
          bindVars = Map(
            "@coll" -> collection.name,
            "path"  -> objectPath.canonicalPath,
            "owner" -> objectPath.owner,
          ),
          options = AqlQueryOptions().streamTransactionId(transactionEntity.getId),
        )
        .runHead

    override def load(folderPath: FolderPath): Task[Seq[ObjectItem]] =
      database
        .query[ObjectItem, ObjectItem](
          query = aqlLoadFolder,
          bindVars = Map(
            "@coll" -> collection.name,
            "path"  -> s"${folderPath.canonicalPath}%",
            "owner" -> folderPath.owner,
          ),
          options = AqlQueryOptions().streamTransactionId(transactionEntity.getId),
        )
        .runCollect

    override def removeFolder(folderPath: FolderPath): Task[Unit] =
      val aql =
        s"""
          |FOR o IN ${collection.name}
          | FILTER o.path LIKE @path
          | FILTER o.owner == @owner
          | REMOVE o._key IN ${collection.name}
          | RETURN {_key: o._key}""".stripMargin

      database
        .query[K, K](
          query = aql,
          bindVars = Map(
            "path"  -> s"${folderPath.canonicalPath}%",
            "owner" -> folderPath.owner,
          ),
          options = AqlQueryOptions().streamTransactionId(transactionEntity.getId),
        )
        .runDrain

    override def removeObject(key: String): Task[Unit] =
      val aql =
        s"""
          |REMOVE @key IN ${collection.name} RETURN OLD""".stripMargin

      database
        .query[K, K](
          query = aql,
          bindVars = Map(
            "key" -> key,
          ),
          options = AqlQueryOptions().streamTransactionId(transactionEntity.getId),
        )
        .runDrain

    override def search(objectPath: ObjectPath): Task[Option[ObjectItem]] =
      database
        .query[ObjectItem, ObjectItem](
          query = aqlSearchObjectItem,
          bindVars = Map(
            "@coll" -> collection.name,
            "path"  -> objectPath.canonicalPath,
            "owner" -> objectPath.owner,
          ),
          options = AqlQueryOptions().streamTransactionId(transactionEntity.getId),
        )
        .runHead

    private def collection = collectionManager.collection

    private def database = collectionManager.database

    override def update(action: UpdateObject): Task[UpdateObject] =
      collection.updateDocument[K, U, K](
        key = action.key,
        value = action,
        options = DocumentUpdateOptions().streamTransactionId(transactionEntity.getId),
      ) as action

  private case class U(
      contentType: String,
      content: Array[Byte],
      updatedAt: ZonedDateTime,
  )
