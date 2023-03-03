package toph.repository

import com.arangodb.model.GeoIndexOptions
import farango.DocumentCollection
import farango.data.Key
import farango.data.given
import farango.query.ForQuery
import farango.zio.given
import io.github.arainko.ducktape.Field
import io.github.arainko.ducktape.into
import org.locationtech.jts.geom.Point
import toph.model.Event
import toph.model.data.CreationInfoData
import toph.query.EventQuery
import toph.query.toQueriableGeometry
import zio.Task
import zio.stream.ZStream

import java.time.ZonedDateTime
import zio.ZIO

trait EventRepository:

  def add(event: Event): Task[Event]

  def remove(key: String): Task[Option[Event]]

  def search(query: EventQuery): ZStream[Any, Throwable, Event]

object EventRepository:

  def apply(collection: DocumentCollection): Task[EventRepository] =
    for _ <- collection.ensureGeoIndex(Seq("position"), GeoIndexOptions().geoJson(true))
    yield Impl(collection)

  private[repository] case class Document(
      _key: Key,
      eventKey: String,
      hypocentreKey: Option[String],
      magnitudeKey: Option[String],
      eventType: Option[String],
      position: Option[Point],
      positionUncertainty: Option[Array[Double]],
      depth: Option[Double],
      depthUncertainty: Option[Double],
      time: Option[ZonedDateTime],
      timeUncertainty: Option[Int],
      stationCount: Option[Int],
      magnitude: Option[Double],
      magnitudeType: Option[String],
      creationInfo: Option[CreationInfoDocument]
  )

  private[repository] case class CreationInfoDocument(
      agencyID: Option[String],
      agencyURI: Option[String],
      author: Option[String],
      creationTime: Option[ZonedDateTime],
      version: Option[String]
  )

  private[repository] given Conversion[Event, Document] = event =>
    event
      .into[Document]
      .transform(
        Field.const(_._key, event.key: Key)
      )

  private[repository] given Conversion[Document, Event] = document =>
    document
      .into[Event]
      .transform(
        Field.const(_.key, document._key: String)
      )

  private class Impl(collection: DocumentCollection) extends EventRepository:

    private def database = collection.database

    override def add(event: Event): Task[Event] =
      collection
        .insert[Document](event)
        .tap(_ => ZIO.logDebug("A QueriableEvent was added."))
        .tapErrorCause(ZIO.logErrorCause("It was impossible to add an QueriableEvent!", _))

    override def remove(key: String): Task[Option[Event]] =
      collection
        .remove[Document](Key.safe(key))
        .tap(_ => ZIO.logDebug(s"QueriableEvent $key was removed."))
        .tapErrorCause(ZIO.logErrorCause(s"It was impossible to remove QueriableEvent $key!", _))

    override def search(query: EventQuery): ZStream[Any, Throwable, Event] =

      var forQuery = query.boundary.foldLeft(ForQuery(collection.name)) { (q, boundary) =>
        q.filter(
          "GEO_DISTANCE(@boundary, d.position) < @distance",
          "boundary" -> toQueriableGeometry(boundary),
          "distance" -> query.boundaryRadius.getOrElse(10000)
        )
      }

      forQuery =
        forQuery.inRangeFilter("d.time", "startTime" -> query.startTime, "endTime" -> query.endTime, true, false)

      forQuery =
        forQuery.inRangeFilter("d.depth", "minDepth" -> query.minDepth, "maxDepth" -> query.maxDepth, true, true)

      forQuery = forQuery.inRangeFilter(
        "d.magnitude",
        "minMagnitude" -> query.minMagnitude,
        "maxMagnitude" -> query.maxMagnitude,
        true,
        true
      )

      forQuery = query.magnitudeType.foldLeft(forQuery) { (q, magnitudeType) =>
        q.filter("POSITION(@magnitudeTypes, d.magnitudeType)", "magnitudeTypes" -> magnitudeType)
      }

      ZStream
        .fromZIO(forQuery[Document](database).query())
        .flatten
