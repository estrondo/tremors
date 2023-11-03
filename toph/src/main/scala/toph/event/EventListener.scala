package toph.event

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

  def apply(eventCentre: EventCentre, keyGenerator: KeyGenerator): EventListener =
    Impl(eventCentre, keyGenerator)

  private class Impl(eventCentre: EventCentre, keyGenerator: KeyGenerator) extends EventListener:

    private val keyGeneratorLayer = ZLayer.succeed(keyGenerator)

    override def apply(event: Event): Task[Event] =
      for
        tophEvent <- TophEventMapper(event)
                       .provideLayer(keyGeneratorLayer)
        _         <- eventCentre
                       .add(tophEvent)(using TophExecutionContext.systemUser[EventListener])
                       .tap(_ => ZIO.logInfo("A new event has been received from Graboid."))
                       .tapErrorCause(ZIO.logErrorCause("Unable to store the event.", _)) @@ ZIOAspect.annotated(
                       "quakeml.eventId" -> event.publicId.resourceId
                     )
      yield event
