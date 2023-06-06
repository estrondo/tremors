package toph.repository

import com.arangodb.model.GeoIndexOptions
import com.softwaremill.macwire.wire
import farango.DocumentCollection
import farango.data.Key
import farango.data.given
import farango.zio.given
import io.github.arainko.ducktape.Field
import io.github.arainko.ducktape.into
import java.time.ZonedDateTime
import org.locationtech.jts.geom.Point
import toph.model.data.HypocentreData
import zio.Task
import zio.ZIO

trait HypocentreDataRepository:

  def add(hypocentre: HypocentreData): Task[HypocentreData]

  def remove(key: String): Task[Option[HypocentreData]]

object HypocentreDataRepository:

  def apply(collection: DocumentCollection): Task[HypocentreDataRepository] =
    for
      indexEntry <- collection.ensureGeoIndex(Seq("position"), GeoIndexOptions().geoJson(true))
      a          <- ZIO.logDebug(s"GeoIndex ${indexEntry.getName()} was created for collection ${collection.name}.")
    yield wire[Impl]

  private[repository] case class Document(
      _key: Key,
      position: Point,
      depth: Option[Double],
      depthUncertainty: Option[Double],
      positionUncertainty: Array[Double],
      time: ZonedDateTime,
      timeUncertainty: Int
  )

  private[repository] given Conversion[HypocentreData, Document] = hypocentre =>
    hypocentre
      .into[Document]
      .transform(Field.const(_._key, hypocentre.key: Key))

  private[repository] given Conversion[Document, HypocentreData] = document =>
    document
      .into[HypocentreData]
      .transform(Field.const(_.key, document._key: String))

  private class Impl(collection: DocumentCollection) extends HypocentreDataRepository:

    override def add(hypocentre: HypocentreData): Task[HypocentreData] =
      collection
        .insert[Document](hypocentre)
        .tap(_ => ZIO.logDebug(s"A new hypocentre was added: ${hypocentre.key}."))
        .tapErrorCause(ZIO.logWarningCause(s"It was impossible to add a new hypocentre: ${hypocentre.key}!", _))

    override def remove(key: String): Task[Option[HypocentreData]] =
      collection
        .remove[Document](Key.safe(key))
        .tap(_ => ZIO.logDebug(s"A hypocentre was removed: $key."))
        .tapErrorCause(ZIO.logWarningCause(s"It was impossible to remove a hypocentre: $key!", _))
