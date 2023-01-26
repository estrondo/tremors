package graboid

import zio.Task
import zio.stream.UStream

import java.time.ZonedDateTime

trait CrawlerScheduler:

  def computeSchedule(
      publisher: Publisher,
      last: CrawlerExecution,
      reference: ZonedDateTime
  ): Task[Iterator[CrawlerExecution]]

  def computeSchedule(
      publisher: Publisher,
      reference: ZonedDateTime
  ): Task[Iterator[CrawlerExecution]]
