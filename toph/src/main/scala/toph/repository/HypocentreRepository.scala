package toph.repository

import com.arangodb.model.GeoIndexOptions
import com.softwaremill.macwire.wire
import farango.DocumentCollection
import farango.data.Key
import farango.data.given
import farango.query.ForQuery
import farango.zio.given
import io.github.arainko.ducktape.Field
import io.github.arainko.ducktape.into
import toph.model.Hypocentre
import toph.query.spatial.SpatialHypocentreQuery
import zio.Task
import zio.ZIO
import zio.stream.ZStream

import java.time.ZonedDateTime

trait HypocentreRepository:

  def add(hypocentre: Hypocentre): Task[Hypocentre]

  def remove(key: String): Task[Option[Hypocentre]]

  def query(query: SpatialHypocentreQuery): ZStream[Any, Throwable, Hypocentre]

object HypocentreRepository:

  def apply(collection: DocumentCollection): Task[HypocentreRepository] =
    for
      indexEntry <- collection.ensureGeoIndex(Seq("position"), GeoIndexOptions().geoJson(false))
      a          <- ZIO.logDebug(s"GeoIndex ${indexEntry.getName()} was created for collection ${collection.name}.")
    yield wire[Impl]

  private[repository] case class Document(
      _key: Key,
      position: Array[Double],
      positionUncertainty: Array[Double],
      time: ZonedDateTime,
      timeUncertainty: Int
  )

  private[repository] given Conversion[Hypocentre, Document] = hypocentre =>
    hypocentre
      .into[Document]
      .transform(Field.const(_._key, hypocentre.key: Key))

  private[repository] given Conversion[Document, Hypocentre] = document =>
    document
      .into[Hypocentre]
      .transform(Field.const(_.key, document._key: String))

  private class Impl(collection: DocumentCollection) extends HypocentreRepository:

    private def database = collection.database

    override def add(hypocentre: Hypocentre): Task[Hypocentre] =
      collection
        .insert[Document](hypocentre)
        .tap(_ => ZIO.logDebug(s"A new hypocentre was added: ${hypocentre.key}."))
        .tapErrorCause(ZIO.logWarningCause(s"It was impossible to add a new hypocentre: ${hypocentre.key}!", _))

    override def remove(key: String): Task[Option[Hypocentre]] =
      collection
        .remove[Document](Key.safe(key))
        .tap(_ => ZIO.logDebug(s"A hypocentre was removed: $key."))
        .tapErrorCause(ZIO.logWarningCause(s"It was impossible to remove a hypocentre: $key!", _))

    override def query(query: SpatialHypocentreQuery): ZStream[Any, Throwable, Hypocentre] =
      var forQuery = ForQuery(collection.name)
        .filter(
          "GEO_DISTANCE(@location, d.position) <= @limit",
          "location" -> query.boundary,
          "limit"    -> query.boundaryRadius.getOrElse(1)
        )

      if query.startTime.isDefined then
        forQuery = forQuery.filter("d.startTime >= @startTime", "startTime" -> query.startTime.get)

      if query.endTime.isDefined then forQuery = forQuery.filter("d.endTime < @endTime", "endTime" -> query.endTime.get)

      if query.minDepth.isDefined then
        forQuery = forQuery.filter("d.depth >= @minDepth", "minDepth" -> query.minDepth.get)

      if query.maxDepth.isDefined then
        forQuery = forQuery.filter("d.depth <= @maxDepth", "maxDepth" -> query.maxDepth.get)

      ZStream
        .fromZIO(forQuery[Document](database).query())
        .flatten
