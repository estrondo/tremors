package toph.repository

import com.arangodb.model.GeoIndexOptions
import com.softwaremill.macwire.wire
import farango.DocumentCollection
import farango.data.ArangoConversion.convertFromKey
import farango.data.ArangoConversion.convertToKey
import farango.data.ArangoConversion.given
import farango.zio.given
import io.github.arainko.ducktape.Field
import io.github.arainko.ducktape.into
import toph.model.Hypocentre
import zio.Task
import zio.ZIO

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

  private[repository] given Conversion[Hypocentre, Document] = hypocentre =>
    hypocentre
      .into[Document]
      .transform(Field.const(_._key, convertToKey(hypocentre.key)))

  private[repository] given Conversion[Document, Hypocentre] = document =>
    document
      .into[Hypocentre]
      .transform(Field.const(_.key, convertFromKey(document._key)))

  private class Impl(collection: DocumentCollection) extends HypocentreRepository:

    override def add(hypocentre: Hypocentre): Task[Hypocentre] =
      collection
        .insert[Document](hypocentre)
        .tap(_ => ZIO.logDebug(s"A new hypocentre was added: ${hypocentre.key}."))
        .tapErrorCause(ZIO.logWarningCause(s"It was impossible to add a new hypocentre: ${hypocentre.key}!", _))

    override def remove(key: String): Task[Option[Hypocentre]] =
      collection
        .remove[Document](convertToKey(key))
        .tap(_ => ZIO.logDebug(s"A hypocentre was removed: $key."))
        .tapErrorCause(ZIO.logWarningCause(s"It was impossible to remove a hypocentre: $key!", _))
