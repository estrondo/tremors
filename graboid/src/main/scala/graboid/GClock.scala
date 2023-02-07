package graboid

import zio.Clock
import zio.UIO

import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

object GClock:

  def currentZonedDateTime(): UIO[ZonedDateTime] =
    for offset <- Clock.currentDateTime
    yield offset.toZonedDateTime().truncatedTo(ChronoUnit.SECONDS)
