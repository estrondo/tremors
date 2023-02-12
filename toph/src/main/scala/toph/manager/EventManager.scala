package toph.manager

import com.softwaremill.macwire.wire
import quakeml.DetectedEvent
import quakeml.{Event => QEvent}
import toph.converter.EventConverter
import toph.model.Epicentre
import toph.model.Event
import toph.model.Hypocentre
import toph.publisher.EventPublisher
import toph.repository.EventRepository
import zio.Task
import zio.ZIO

trait EventManager:
  def accept(event: DetectedEvent): Task[(Event, Seq[(Epicentre, Option[Hypocentre])])]

object EventManager:

  def apply(
      repository: EventRepository,
      spatialManager: SpatialManager,
      magnitudeManager: MagnitudeManager
  ): EventManager =
    wire[Impl]

  private class Impl(repository: EventRepository, spatialManager: SpatialManager, magnitudeManager: MagnitudeManager)
      extends EventManager:
    override def accept(detected: DetectedEvent): Task[(Event, Seq[(Epicentre, Option[Hypocentre])])] =
      (for
        event      <- EventConverter.fromQEvent(detected.event)
        _          <- add(event)
        origins    <- ZIO.foreach(detected.event.origin)(spatialManager.accept)
        magnitudes <- ZIO.foreach(detected.event.magnitude)(magnitudeManager.accept)
      yield (event, origins))
        .tap(_ => ZIO.logInfo(s"A detected with key=${detected.event.publicID.uri} event was accepted."))
        .tapErrorCause(
          ZIO.logWarningCause(
            s"It was impossible to accept a detected event with key=${detected.event.publicID.uri}!",
            _
          )
        )

    private def add(event: Event): Task[Event] =
      repository
        .add(event)
        .tap(_ => ZIO.logDebug(s"A new event with key=${event.key} was added."))
        .tapErrorCause(ZIO.logWarningCause(s"It was impossible to add event with key=${event.key}!", _))
