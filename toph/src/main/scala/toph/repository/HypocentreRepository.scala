package toph.repository

import com.softwaremill.macwire.wire
import farango.DocumentCollection
import farango.data.ArangoConversion.given
import farango.zio.given
import io.github.arainko.ducktape.Field
import io.github.arainko.ducktape.into
import toph.model.Hypocentre
import zio.Task
import zio.ZIO
import com.arangodb.model.GeoIndexOptions

trait HypocentreRepository:

  def add(hypocentre: Hypocentre): Task[Hypocentre]

  def remove(key: String): Task[Option[Hypocentre]]

object HypocentreRepository:

  def apply(collection: DocumentCollection): Task[HypocentreRepository] =
    for
      indexEntry <- collection.ensureGeoIndex(Seq("position"), GeoIndexOptions().geoJson(false))
      a          <- ZIO.logDebug(s"GeoIndex ${indexEntry.getName()} was created for collection ${collection.name}.")
    yield wire[Impl]

  private[repository] case class Document(
      _key: String,
      position: Array[Double],
      positionUncertainty: Array[Double],
      time: Long,
      timeUncertainty: Int
  )

  private[repository] given Conversion[Hypocentre, Document] =
    _.into[Document]
      .transform(Field.renamed(_._key, _.key))

  private[repository] given Conversion[Document, Hypocentre] =
    _.into[Hypocentre]
      .transform(Field.renamed(_.key, _._key))

  private class Impl(collection: DocumentCollection) extends HypocentreRepository:

    override def add(hypocentre: Hypocentre): Task[Hypocentre] =
      collection
        .insert[Document](hypocentre)
        .tap(_ => ZIO.logDebug(s"A new hypocentre was added: ${hypocentre.key}."))
        .tapErrorCause(ZIO.logWarningCause(s"It was impossible to add a new hypocentre: ${hypocentre.key}!", _))

    override def remove(key: String): Task[Option[Hypocentre]] =
      collection
        .remove[Document](key)
        .tap(_ => ZIO.logDebug(s"A hypocentre was removed: $key."))
        .tapErrorCause(ZIO.logWarningCause(s"It was impossible to remove a hypocentre: $key!", _))
