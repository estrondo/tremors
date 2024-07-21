package toph

import java.time.Clock
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

trait TimeService:
  def zonedDateTimeNow(): ZonedDateTime

object TimeService extends TimeService:

  private val clock = Clock.systemUTC()

  override def zonedDateTimeNow(): ZonedDateTime =
    ZonedDateTime.now(clock).truncatedTo(ChronoUnit.MILLIS)
