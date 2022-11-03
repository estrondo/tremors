package borercodec

import io.bullet.borer.Codec

import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZonedDateTime

val ZoneId = Clock.systemUTC().getZone()

given Codec[Duration] =
  Codec.bimap[Long, Duration](_.toSeconds(), seconds => Duration.ofSeconds(seconds))

given Codec[ZonedDateTime] =
  Codec.bimap[Long, ZonedDateTime](
    _.toEpochSecond(),
    epochSeconds => ZonedDateTime.ofInstant(Instant.ofEpochSecond(epochSeconds), ZoneId)
  )
