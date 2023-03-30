package graboid

import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import zio.Clock
import zio.UIO

object GClock:

  def currentZonedDateTime(): UIO[ZonedDateTime] =
    for offset <- Clock.currentDateTime
    yield offset.toZonedDateTime().truncatedTo(ChronoUnit.SECONDS)
