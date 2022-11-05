package graboid.arango

import java.time.temporal.ChronoUnit
import java.time.temporal.ChronoField
import java.time.ZonedDateTime
import java.time.Clock
import java.time.Instant
import java.time.Duration

object ArangoConversion:

  val ZoneId = Clock.systemUTC().getZone()

  given Conversion[ZonedDateTime, Long] = _.getLong(ChronoField.INSTANT_SECONDS)

  given Conversion[Long, ZonedDateTime] = longValue =>
    ZonedDateTime.ofInstant(Instant.ofEpochSecond(longValue), ZoneId)

  given Conversion[Long, Duration] = Duration.ofSeconds(_)

  given Conversion[Duration, Long] = _.getSeconds()
