package toph.manager

import com.softwaremill.macwire.wire
import core.KeyGenerator
import quakeml.QuakeMLOrigin
import toph.converter.HypocentreDataConverter
import toph.model.Event
import toph.model.data.EventData
import toph.model.data.HypocentreData
import toph.model.data.MagnitudeData
import toph.query.EventQuery
import toph.repository.HypocentreDataRepository
import toph.repository.EventRepository
import zio.Task
import zio.UIO
import zio.ZIO
import zio.stream.ZStream

import scala.collection.immutable.HashMap
import scala.collection.immutable.HashSet

trait SpatialManager:

  def accept(origin: QuakeMLOrigin): Task[HypocentreData]

  def createEvents(
      eventData: EventData,
      hypocentres: Seq[HypocentreData],
      magnitudes: Seq[MagnitudeData]
  ): Task[Seq[Event]]

  def search(query: EventQuery): ZStream[Any, Throwable, Event]

object SpatialManager:

  private type ProtoEvent = (EventData, Option[HypocentreData], Option[MagnitudeData])

  def apply(
      hypocentreRepository: HypocentreDataRepository,
      eventRepository: EventRepository,
      keyGenerator: KeyGenerator
  ): SpatialManager =
    wire[Impl]

  private class Impl(
      hypocentreRepository: HypocentreDataRepository,
      eventRepository: EventRepository,
      keyGenerator: KeyGenerator
  ) extends SpatialManager:

    override def accept(origin: QuakeMLOrigin): Task[HypocentreData] =
      for hypocentre <- createHypocentre(origin)
      yield hypocentre

    private def createHypocentre(origin: QuakeMLOrigin): Task[HypocentreData] =
      (for
        hypocentre <- HypocentreDataConverter.from(origin)
        _          <- hypocentreRepository.add(hypocentre)
      yield hypocentre)
        .tap(_ => ZIO.logDebug(s"An hypocentre for origin=${origin.publicID.uri} was added."))
        .tapErrorCause(
          ZIO.logWarningCause(s"It was impossible to add a hypocentre for origin=${origin.publicID.uri}!", _)
        )

    override def createEvents(
        eventData: EventData,
        hypocentres: Seq[HypocentreData],
        magnitudes: Seq[MagnitudeData]
    ): Task[Seq[Event]] =
      for
        protoEvents <- createProtoEvents(eventData, hypocentres, magnitudes)
        result      <- ZIO.foreach(protoEvents)(createEvent)
      yield result

    private def createEvent(
        eventData: EventData,
        hypocentre: Option[HypocentreData],
        magnitude: Option[MagnitudeData]
    ): Task[Event] =
      var event = Event(
        key = keyGenerator.next32(),
        eventKey = eventData.key,
        creationInfo = eventData.creationInfo,
        hypocentreKey = None,
        magnitudeKey = None,
        eventType = eventData.`type`,
        position = None,
        positionUncertainty = None,
        depth = None,
        depthUncertainty = None,
        time = None,
        timeUncertainty = None,
        stationCount = None,
        magnitude = None,
        magnitudeType = None
      )

      event = hypocentre.foldLeft(event) { (event, hypocentre) =>
        event.copy(
          hypocentreKey = Some(hypocentre.key),
          position = Some(hypocentre.position),
          positionUncertainty = Some(hypocentre.positionUncertainty),
          depth = hypocentre.depth,
          depthUncertainty = hypocentre.depthUncertainty,
          time = Some(hypocentre.time),
          timeUncertainty = Some(hypocentre.timeUncertainty)
        )
      }

      event = magnitude.foldLeft(event) { (event, magnitude) =>
        event.copy(
          magnitudeKey = Some(magnitude.key),
          magnitude = Some(magnitude.mag),
          magnitudeType = magnitude.`type`,
          stationCount = magnitude.stationCount
        )
      }

      eventRepository
        .add(event)
        .tap(_ => ZIO.logDebug("A new event was added."))
        .tapErrorCause(ZIO.logErrorCause("It was impossible to add a event!", _))

    private def createProtoEvents(
        event: EventData,
        hypocentres: Seq[HypocentreData],
        magnitudes: Seq[MagnitudeData]
    ): UIO[Seq[ProtoEvent]] = ZIO.succeed {
      val hypocentreMap = HashMap.from(hypocentres.view.map(x => (x.key, x)))
      val magnitudeMap  = HashMap.from(magnitudes.view.map(x => (x.key, x)))

      val preferredHypocentre = event.preferredOriginKey.flatMap(hypocentreMap.get)
      val preferredMagnitude  = event.preferedMagnitudeKey.flatMap(magnitudeMap.get)

      val hypocentreKeys = event.originKey.to(HashSet) -- event.preferredOriginKey
      val magnitudeKeys  = event.magnitudeKey.to(HashSet) -- event.preferedMagnitudeKey

      val eventWithHypocentres = hypocentreKeys.view
        .map(hypocentreMap.get)
        .collect({ case Some(value) => (event, Some(value), None) })

      val eventWithMagnitudes = magnitudeKeys.view
        .map(magnitudeMap.get)
        .collect({ case Some(value) => (event, None, Some(value)) })

      Seq((event, preferredHypocentre, preferredMagnitude)) ++ eventWithHypocentres ++ eventWithMagnitudes
    }

    override def search(query: EventQuery): ZStream[Any, Throwable, Event] =
      ZStream.logDebug("Searching for Events.") *> eventRepository.search(query)
