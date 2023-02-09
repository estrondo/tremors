package toph.manager

import com.softwaremill.macwire.wire
import quakeml.DetectedEvent
import quakeml.{Event => QEvent}
import toph.converter.EventConverter
import toph.model.Event
import toph.publisher.EventPublisher
import toph.repository.EventRepository
import zio.Task
import zio.ZIO
import toph.model.Epicentre
import toph.model.Hypocentre

trait EventManager:
  def accept(event: DetectedEvent): Task[(Event, Seq[(Epicentre, Option[Hypocentre])])]

object EventManager:

  def apply(repository: EventRepository, spatialManager: SpatialManager): EventManager =
    wire[Impl]

  private class Impl(repository: EventRepository, spatialManager: SpatialManager) extends EventManager:
    override def accept(detected: DetectedEvent): Task[(Event, Seq[(Epicentre, Option[Hypocentre])])] =
      for
        event   <- EventConverter.fromQEvent(detected.event)
        _       <- add(event)
        origins <- ZIO.foreach(detected.event.origin)(spatialManager.accept)
      yield (event, origins)

    private def add(event: Event): Task[Event] =
      repository
        .add(event)
        .tap(_ => ZIO.logDebug(s"A new event with key=${event.key} was added."))
        .tapErrorCause(ZIO.logWarningCause(s"It was impossible to add event with key=${event.key}!", _))
