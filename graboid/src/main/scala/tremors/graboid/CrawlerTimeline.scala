package tremors.graboid

import zio.Task

import java.time.ZonedDateTime

trait CrawlerTimeline:

  def lastUpdate: Task[Option[ZonedDateTime]]
