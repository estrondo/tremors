package webapi

import zio.Clock
import zio.UIO

import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.time.{Clock => JClock}

extension (clock: Clock.type)
  def currentZonedDateTime(): UIO[ZonedDateTime] =
    for offset <- clock.currentDateTime
    yield offset
      .atZoneSameInstant(JClock.systemUTC().getZone())
      .truncatedTo(ChronoUnit.SECONDS)
