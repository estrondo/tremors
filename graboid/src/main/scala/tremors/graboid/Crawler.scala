package tremors.graboid

import tremors.graboid.quakeml.model.Event
import tremors.graboid.quakeml.model.Magnitude
import tremors.graboid.quakeml.model.Origin
import zio.stream.ZStream
import zio.Task

object Crawler:

  type Info = Event | Origin | Magnitude
  type Stream = ZStream[Any, Throwable, Crawler.Info]

trait Crawler:

  def crawl(): Task[Crawler.Stream]
