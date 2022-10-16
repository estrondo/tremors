package tremors.graboid

import tremors.graboid.repository.TimelineRepository
import zio.Task
import zio.TaskLayer

import java.time.Duration
import java.time.ZonedDateTime
import java.util.UUID

import TimelineManager.*

trait TimelineManager:

  def nextWindow(name: String): Task[TimelineManager.Window]

  def register(name: String, window: TimelineManager.Window): Task[TimelineManager.Window]

object TimelineManager:

  type Layer = TaskLayer[TimelineManager]

  case class Config(
      defaultWindowDuration: Duration,
      startingInstant: ZonedDateTime
  )

  case class Window(id: String, beginning: ZonedDateTime, ending: ZonedDateTime)

  def apply(config: Config, repository: TimelineRepository): TimelineManager =
    TimelineManagerImpl(config, repository)

private[graboid] class TimelineManagerImpl(
    config: TimelineManager.Config,
    repository: TimelineRepository
) extends TimelineManager:

  private def nextID(): String = UUID.randomUUID().toString()

  override def nextWindow(name: String): Task[Window] =
    for lastWindow <- repository.last(name)
    yield computeNextWindow(lastWindow)

  override def register(name: String, window: Window): Task[Window] =
    repository.add(name, window)

  private def computeNextWindow(option: Option[Window]): Window =
    option match
      case Some(Window(_, _, ending)) =>
        Window(nextID(), ending, ending.plus(config.defaultWindowDuration))
      case _                          =>
        Window(
          nextID(),
          config.startingInstant,
          config.startingInstant.plus(config.defaultWindowDuration)
        )
