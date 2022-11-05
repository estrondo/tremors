package graboid

import _root_.quakeml.Event
import _root_.quakeml.Magnitude
import _root_.quakeml.Origin
import zio.Task
import zio.stream.ZStream

import java.time.ZonedDateTime
import _root_.quakeml.CreationInfo

object Crawler:

  type Info   = Event | Origin | Magnitude
  type Stream = ZStream[Any, Throwable, Crawler.Info]

trait Crawler:

  def crawl(window: TimelineManager.Window): Task[Crawler.Stream]
