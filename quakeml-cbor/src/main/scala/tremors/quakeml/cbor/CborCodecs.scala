package tremors.quakeml.cbor

import io.bullet.borer.Codec
import io.bullet.borer.Decoder
import io.bullet.borer.Encoder
import io.bullet.borer.derivation.MapBasedCodecs.*
import tremors.quakeml.Comment
import tremors.quakeml.CreationInfo
import tremors.quakeml.EvaluationMode
import tremors.quakeml.EvaluationStatus
import tremors.quakeml.Event
import tremors.quakeml.Magnitude
import tremors.quakeml.RealQuantity
import tremors.quakeml.ResourceReference

import java.time.Clock
import java.time.Instant
import java.time.ZonedDateTime
import java.time.temporal.ChronoField
import scala.util.Try

private[cbor] val ZoneId = Clock.systemUTC().getZone()

given Encoder[ZonedDateTime] =
  Encoder.forLong.contramap(_.getLong(ChronoField.INSTANT_SECONDS))

given Decoder[ZonedDateTime] =
  Decoder.forLong.map(value => ZonedDateTime.ofInstant(Instant.ofEpochSecond(value), ZoneId))

given Codec[ZonedDateTime] =
  Codec(summon[Encoder[ZonedDateTime]], summon[Decoder[ZonedDateTime]])

given Codec[EvaluationMode]        = deriveCodec[EvaluationMode]
given Codec[EvaluationStatus]      = deriveCodec[EvaluationStatus]
given Codec[RealQuantity]          = deriveCodec[RealQuantity]
given Codec[Magnitude]             = deriveCodec[Magnitude]
given Codec[ResourceReference]     = deriveCodec[ResourceReference]
given Codec[Event.Type]            = deriveCodec[Event.Type]
given Codec[Event.TypeCertainty]   = deriveCodec[Event.TypeCertainty]
given Codec[Event.DescriptionType] = deriveCodec[Event.DescriptionType]
given Codec[Event.Description]     = deriveCodec[Event.Description]
given Codec[CreationInfo]          = deriveCodec[CreationInfo]
given Codec[Comment]               = deriveCodec[Comment]
given Codec[Event]                 = deriveCodec[Event]
