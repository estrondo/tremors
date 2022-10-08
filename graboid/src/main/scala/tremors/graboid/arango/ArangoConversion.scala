package tremors.graboid.arango

import java.time.temporal.ChronoUnit
import java.time.temporal.ChronoField
import java.time.ZonedDateTime
import java.time.Clock
import java.time.Instant

object ArangoConversion:

  val ZoneId = Clock.systemUTC().getZone()

  given Conversion[ZonedDateTime, Long] = _.getLong(ChronoField.INSTANT_SECONDS)

  given Conversion[Long, ZonedDateTime] = longValue =>
    ZonedDateTime.ofInstant(Instant.ofEpochSecond(longValue), ZoneId)
