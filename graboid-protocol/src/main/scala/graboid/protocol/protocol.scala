package graboid.protocol

import io.bullet.borer.Decoder
import io.bullet.borer.Encoder
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZonedDateTime

given Encoder[ZonedDateTime] = Encoder.forLong.contramap[ZonedDateTime](_.toEpochSecond)

given Decoder[ZonedDateTime] =
  Decoder.forLong.map(second => ZonedDateTime.ofInstant(Instant.ofEpochSecond(second), Clock.systemUTC().getZone))

given Encoder[Duration] = Encoder.forLong.contramap[Duration](_.toSeconds)

given Decoder[Duration] = Decoder.forLong.map(Duration.ofSeconds)
