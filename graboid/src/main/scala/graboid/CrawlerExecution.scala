package graboid

import java.time.ZonedDateTime

object CrawlerExecution:

  given Conversion[Status, Int]     = _.ordinal
  given Conversion[Int, Status]     = Status.fromOrdinal(_)
  given Conversion[Status, Integer] = _.ordinal
  given Conversion[Integer, Status] = Status.fromOrdinal(_)

  enum Status:
    case Running
    case Completed
    case Failed
    case Cancelled

case class CrawlerExecution(
    key: String,
    publisherKey: String,
    beginning: ZonedDateTime,
    ending: ZonedDateTime,
    status: Option[CrawlerExecution.Status] = None,
    executionStarted: Option[ZonedDateTime] = None,
    expectedStop: Option[ZonedDateTime] = None,
    executionStopped: Option[ZonedDateTime] = None,
    message: Option[String] = None
)
