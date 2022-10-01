package tremors.graboid

import tremors.graboid.repository.TimelineRepository
import zio.Task

import java.time.Duration
import java.time.ZonedDateTime

import TimelineManager.*

trait TimelineManager:

  def nextWindow(
      name: String,
      duration: Duration,
      reference: ZonedDateTime
  ): Task[TimelineManager.Window]

object TimelineManager:

  type Window = (Option[ZonedDateTime], Option[ZonedDateTime])

private[graboid] class TimelineManagerImpl(repository: TimelineRepository) extends TimelineManager:

  override def nextWindow(
      name: String,
      duration: Duration,
      reference: ZonedDateTime
  ): Task[Window] = ???
