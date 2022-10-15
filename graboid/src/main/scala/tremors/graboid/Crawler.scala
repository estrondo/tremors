package tremors.graboid

import tremors.quakeml.Event
import tremors.quakeml.Magnitude
import tremors.quakeml.Origin
import zio.Task
import zio.stream.ZStream

import java.time.ZonedDateTime
import tremors.quakeml.CreationInfo

object Crawler:

  type Info   = Event | Origin | Magnitude
  type Stream = ZStream[Any, Throwable, Crawler.Info]

trait Crawler:

  def crawl(window: TimelineManager.Window): Task[Crawler.Stream]
