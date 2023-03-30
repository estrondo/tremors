package webapi

import java.time.{Clock => JClock}
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import zio.Clock
import zio.UIO

extension (clock: Clock.type)
  def currentZonedDateTime(): UIO[ZonedDateTime] =
    for offset <- clock.currentDateTime
    yield offset
      .atZoneSameInstant(JClock.systemUTC().getZone())
      .truncatedTo(ChronoUnit.SECONDS)
