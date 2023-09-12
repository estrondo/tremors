package graboid

import java.time.ZonedDateTime

case class CrawlingExecution(
    id: String,
    dataCentreId: String,
    createdAt: ZonedDateTime,
    updatedAt: Option[ZonedDateTime],
    starting: ZonedDateTime,
    ending: ZonedDateTime,
    detected: Long,
    state: CrawlingExecution.State
)

object CrawlingExecution:

  enum State:
    case Starting, Running, Completed, Failed
