package tremors.graboid.quakeml

import zio.stream.ZStream

object EventCrawler:

  type Info = Event

trait EventCrawler:

  def crawl(): ZStream[Any, Throwable, EventCrawler.Info]
