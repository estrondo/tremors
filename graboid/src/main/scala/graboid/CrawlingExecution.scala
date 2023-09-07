package graboid

import java.time.ZonedDateTime

case class CrawlingExecution(
    id: String,
    startedAt: ZonedDateTime,
    updatedAt: Option[ZonedDateTime],
    succeed: Long,
    failed: Long,
    state: CrawlingExecution.State
)

object CrawlingExecution:

  enum State:
    case Starting, Running, Completed, Failed
