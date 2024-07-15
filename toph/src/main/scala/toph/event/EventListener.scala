package toph.event

import org.locationtech.jts.geom.GeometryFactory
import toph.centre.EventCentre
import toph.context.TophExecutionContext
import toph.model.TophEventMapper
import tremors.generator.KeyGenerator
import tremors.quakeml.Event
import zio.Task
import zio.ZIO
import zio.ZIOAspect
import zio.ZLayer

trait EventListener:

  def apply(event: Event): Task[Event]

object EventListener:

  def apply(eventCentre: EventCentre, keyGenerator: KeyGenerator, geometryFactory: GeometryFactory): EventListener =
    Impl(eventCentre, keyGenerator, geometryFactory)

  private class Impl(eventCentre: EventCentre, keyGenerator: KeyGenerator, geometryFactory: GeometryFactory)
      extends EventListener:

    private def supportLayer = ZLayer.succeed(keyGenerator) ++ ZLayer.succeed(geometryFactory)

    override def apply(event: Event): Task[Event] =
      for
        tophEvents <- TophEventMapper(event).provideLayer(supportLayer)
        _          <- ZIO.foreach(tophEvents) { tophEvent =>
                        eventCentre
                          .add(tophEvent)(using TophExecutionContext.systemUser[EventListener])
                          .tap(_ => ZIO.logInfo("A new event has been received from Graboid."))
                          .tapErrorCause(ZIO.logErrorCause("Unable to store the event.", _)) @@ ZIOAspect.annotated(
                          "quakeml.eventId" -> event.publicId.resourceId
                        )
                      }
      yield event
