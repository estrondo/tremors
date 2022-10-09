package tremors.graboid

import tremors.graboid.repository.TimelineRepository
import zio.Task

import java.time.Duration
import java.time.ZonedDateTime

import TimelineManager.*

trait TimelineManager:

  def nextWindow(name: String): Task[TimelineManager.Window]

object TimelineManager:

  type Window = (ZonedDateTime, ZonedDateTime)

private[graboid] class TimelineManagerImpl(repository: TimelineRepository) extends TimelineManager:

  override def nextWindow(name: String): Task[Window] =
    for lastWindow <- repository.last(name)
    yield ???
