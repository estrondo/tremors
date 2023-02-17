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
import org.locationtech.jts.geom.Point
import toph.model.Epicentre
import toph.query.spatial.SpatialEpicentreQuery
import toph.query.spatial.toQueriableGeometry
import zio.Task
import zio.ZIO
import zio.stream.ZStream

import java.time.ZonedDateTime

trait EpicentreRepository:

  def add(epicentre: Epicentre): Task[Epicentre]

  def remove(key: String): Task[Option[Epicentre]]

  def query(query: SpatialEpicentreQuery): ZStream[Any, Throwable, Epicentre]

object EpicentreRepository:

  def apply(collection: DocumentCollection): Task[EpicentreRepository] =
    for
      indexEntry <- collection.ensureGeoIndex(Seq("position"), GeoIndexOptions().geoJson(true))
      a          <- ZIO.logDebug(s"GeoIndex ${indexEntry.getName()} was created for collection ${collection.name}.")
    yield wire[Impl]

  private[repository] case class Document(
      _key: Key,
      position: Point,
      positionUncertainty: Array[Double],
      time: ZonedDateTime,
      timeUncertainty: Int
  )

  private[repository] given Conversion[Epicentre, Document] = epicentre =>
    epicentre
      .into[Document]
      .transform(Field.const(_._key, epicentre.key: Key))

  private[repository] given Conversion[Document, Epicentre] = document =>
    document
      .into[Epicentre]
      .transform(Field.const(_.key, document._key: String))

  private class Impl(collection: DocumentCollection) extends EpicentreRepository:

    private def database = collection.database

    override def add(epicentre: Epicentre): Task[Epicentre] =
      collection
        .insert[Document](epicentre)
        .tap(_ => ZIO.logDebug(s"A new epicentre was added: ${epicentre.key}."))
        .tapErrorCause(ZIO.logWarningCause(s"It was impossible to add an epicentre: ${epicentre.key}!", _))

    override def remove(key: String): Task[Option[Epicentre]] =
      collection
        .remove[Document](Key.safe(key))
        .tap(_ => ZIO.logDebug(s"An epicentre was removed: $key."))
        .tapErrorCause(ZIO.logWarningCause(s"It was impossible to remove an epicentre: $key!", _))

    override def query(query: SpatialEpicentreQuery): ZStream[Any, Throwable, Epicentre] =
      var forQuery = ForQuery(collection.name)
        .filter(
          "GEO_DISTANCE(@position, d.position) <= @limit",
          "position" -> toQueriableGeometry(query.boundary),
          "limit"    -> query.boundaryRadius.getOrElse(1)
        )

      if query.startTime.isDefined then
        forQuery = forQuery.filter("d.time >= @startTime", "startTime" -> query.startTime.get)

      if query.endTime.isDefined then forQuery = forQuery.filter("d.time < @endTime", "endTime" -> query.endTime.get)

      ZStream
        .fromZIO(forQuery[Document](database).query())
        .flatten
