package graboid.repository

import com.arangodb.model.DocumentCreateOptions
import com.arangodb.model.DocumentDeleteOptions
import com.arangodb.model.DocumentUpdateOptions
import com.softwaremill.macwire.wire
import graboid.FDSNDataCentre
import io.github.arainko.ducktape.Field
import one.estrondo.farango.Collection
import one.estrondo.farango.FarangoTransformer
import one.estrondo.farango.ducktape.DucktapeTransformer
import one.estrondo.farango.ducktape.given
import one.estrondo.farango.zio.given
import zio.Task
import zio.stream.ZStream

trait FDSNDataCentreRepository:

  def insert(dataCentre: FDSNDataCentre): Task[FDSNDataCentre]

  def update(dataCentre: FDSNDataCentre): Task[FDSNDataCentre]

  def delete(id: String): Task[FDSNDataCentre]

  def get(id: String): Task[Option[FDSNDataCentre]]

  def all: ZStream[Any, Throwable, FDSNDataCentre]

object FDSNDataCentreRepository:

  private val QueryAll = "FOR d IN @@collection SORT d._key ASC RETURN d"

  def apply(collection: Collection): FDSNDataCentreRepository =
    wire[Impl]

  private given FarangoTransformer[FDSNDataCentre, Stored] = DucktapeTransformer(
    Field.renamed(_._key, _.id)
  )

  private given FarangoTransformer[Stored, FDSNDataCentre] = DucktapeTransformer(
    Field.renamed(_.id, _._key)
  )

  private case class Stored(_key: String, url: String)

  private case class Update(url: String)

  private class Impl(collection: Collection) extends FDSNDataCentreRepository:

    override def insert(dataCentre: FDSNDataCentre): Task[FDSNDataCentre] =
      for entry <- collection.insertDocument[Stored, FDSNDataCentre](
                     dataCentre,
                     DocumentCreateOptions().returnNew(true)
                   )
      yield entry.getNew

    override def update(dataCentre: FDSNDataCentre): Task[FDSNDataCentre] =
      for entry <- collection.updateDocument[Stored, Update, FDSNDataCentre](
                     dataCentre.id,
                     dataCentre,
                     DocumentUpdateOptions().returnNew(true)
                   )
      yield entry.getNew

    override def delete(id: String): Task[FDSNDataCentre] =
      for entry <- collection.deleteDocument[Stored, FDSNDataCentre](id, DocumentDeleteOptions().returnOld(true))
      yield entry.getOld

    override def get(id: String): Task[Option[FDSNDataCentre]] =
      collection.getDocument[Stored, FDSNDataCentre](id)

    override def all: ZStream[Any, Throwable, FDSNDataCentre] =
      collection.database.query[Stored, FDSNDataCentre](QueryAll, Map("@collection" -> collection.name))
