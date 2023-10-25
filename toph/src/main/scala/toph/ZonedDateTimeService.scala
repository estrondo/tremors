package toph

import java.time.ZonedDateTime

trait ZonedDateTimeService:
  def now(): ZonedDateTime
