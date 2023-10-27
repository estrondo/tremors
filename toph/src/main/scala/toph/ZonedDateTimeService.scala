package toph

import java.time.Clock
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

trait ZonedDateTimeService:
  def now(): ZonedDateTime

object ZonedDateTimeService extends ZonedDateTimeService:

  private val clock = Clock.systemUTC()

  override def now(): ZonedDateTime =
    ZonedDateTime.now(clock).truncatedTo(ChronoUnit.NANOS)
