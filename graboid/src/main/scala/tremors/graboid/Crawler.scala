package tremors.graboid

import zio.stream.Stream
import zio.UIO
import tremors.graboid.quakeml.Event
import tremors.graboid.quakeml.Origin
import tremors.graboid.quakeml.Magnitude

object Crawler:

  type Info = Event | Origin | Magnitude

trait Crawler:

  type EventStream = Stream[Throwable, Crawler.Info]

  def crawl(): UIO[EventStream]
