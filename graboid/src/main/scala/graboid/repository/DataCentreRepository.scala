package graboid.repository

import com.arangodb.model.DocumentCreateOptions
import com.arangodb.model.DocumentDeleteOptions
import com.arangodb.model.DocumentUpdateOptions
import graboid.DataCentre
import io.github.arainko.ducktape.Field
import one.estrondo.farango.FarangoTransformer
import one.estrondo.farango.ducktape.DucktapeTransformer
import one.estrondo.farango.ducktape.given
import one.estrondo.farango.zio.given
import tremors.zio.farango.CollectionManager
import zio.Task
import zio.stream.ZStream

trait DataCentreRepository:

  def insert(dataCentre: DataCentre): Task[DataCentre]

  def update(dataCentre: DataCentre): Task[DataCentre]

  def delete(id: String): Task[DataCentre]

  def get(id: String): Task[Option[DataCentre]]

  def all: ZStream[Any, Throwable, DataCentre]

object DataCentreRepository:

  private val QueryAll = "FOR d IN @@collection SORT d._key ASC RETURN d"

  def apply(collectionManager: CollectionManager): DataCentreRepository =
    Impl(collectionManager)

  private given FarangoTransformer[DataCentre, Stored] = DucktapeTransformer(
    Field.renamed(_._key, _.id),
  )

  private given FarangoTransformer[Stored, DataCentre] = DucktapeTransformer(
    Field.renamed(_.id, _._key),
  )

  private case class Stored(_key: String, event: Option[String], dataselect: Option[String])

  private case class Update(event: Option[String], dataselect: Option[String])

  private class Impl(collectionManager: CollectionManager) extends DataCentreRepository:

    private val collection = collectionManager.collection

    override def insert(dataCentre: DataCentre): Task[DataCentre] =
      for entry <- collection
                     .insertDocument[Stored, DataCentre](
                       dataCentre,
                       DocumentCreateOptions().returnNew(true),
                     )
                     .retry(collectionManager.sakePolicy)
      yield entry.getNew

    override def update(dataCentre: DataCentre): Task[DataCentre] =
      for entry <- collection
                     .updateDocument[Stored, Update, DataCentre](
                       dataCentre.id,
                       dataCentre,
                       DocumentUpdateOptions().returnNew(true),
                     )
                     .retry(collectionManager.sakePolicy)
      yield entry.getNew

    override def delete(id: String): Task[DataCentre] =
      for entry <- collection
                     .deleteDocument[Stored, DataCentre](id, DocumentDeleteOptions().returnOld(true))
                     .retry(collectionManager.sakePolicy)
      yield entry.getOld

    override def get(id: String): Task[Option[DataCentre]] =
      collection.getDocument[Stored, DataCentre](id).retry(collectionManager.sakePolicy)

    override def all: ZStream[Any, Throwable, DataCentre] =
      collection.database
        .query[Stored, DataCentre](QueryAll, Map("@collection" -> collection.name))
        .retry(collectionManager.sakePolicy)
