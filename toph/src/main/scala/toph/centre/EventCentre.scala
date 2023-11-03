package toph.centre

import toph.context.TophExecutionContext
import toph.model.TophEvent
import toph.repository.EventRepository
import zio.Task
import zio.ZIO
import zio.ZIOAspect

trait EventCentre:

  def add(event: TophEvent)(using TophExecutionContext): Task[TophEvent]

object EventCentre:

  def apply(repository: EventRepository): EventCentre =
    Impl(repository)

  private class Impl(repository: EventRepository) extends EventCentre:

    override def add(event: TophEvent)(using TophExecutionContext): Task[TophEvent] =
      repository
        .add(event)
        .tap(_ => ZIO.logInfo("New event has been added."))
        .tapErrorCause(ZIO.logWarningCause("Unable to add event.", _)) @@ ZIOAspect.annotated(
        "eventCentre.eventId" -> event.id
      )
