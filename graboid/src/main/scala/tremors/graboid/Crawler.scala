package tremors.graboid

import tremors.graboid.quakeml.model.Event
import tremors.graboid.quakeml.model.Magnitude
import tremors.graboid.quakeml.model.Origin
import zio.Task
import zio.stream.ZStream

import java.time.ZonedDateTime
import tremors.graboid.quakeml.model.CreationInfo

object Crawler:

  type Info   = Event | Origin | Magnitude
  type Stream = ZStream[Any, Throwable, Crawler.Info]

trait Crawler:

  def crawl(window: TimelineManager.Window): Task[Crawler.Stream]
