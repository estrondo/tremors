package cbor.quakeml

import cbor.core.given
import io.bullet.borer.Codec
import io.bullet.borer.Decoder
import io.bullet.borer.Encoder
import io.bullet.borer.derivation.MapBasedCodecs.*
import quakeml.Comment
import quakeml.CreationInfo
import quakeml.DetectedEvent
import quakeml.EvaluationMode
import quakeml.EvaluationStatus
import quakeml.Event
import quakeml.Magnitude
import quakeml.Origin
import quakeml.Origin.DepthType
import quakeml.RealQuantity
import quakeml.ResourceReference
import quakeml.TimeQuantity

import java.time.Clock
import java.time.Instant
import java.time.ZonedDateTime
import java.time.temporal.ChronoField
import scala.util.Try

private[cbor] val ZoneId = Clock.systemUTC().getZone()

given Codec[EvaluationMode]          = deriveCodec
given Codec[EvaluationStatus]        = deriveCodec
given Codec[RealQuantity]            = deriveCodec
given Codec[Magnitude]               = deriveCodec
given Codec[ResourceReference]       = deriveCodec
given eventType: Codec[Event.Type]   = deriveCodec
given Codec[Event.TypeCertainty]     = deriveCodec
given Codec[Event.DescriptionType]   = deriveCodec
given Codec[Event.Description]       = deriveCodec
given Codec[CreationInfo]            = deriveCodec
given Codec[Comment]                 = deriveCodec
given Codec[Event]                   = deriveCodec
given Codec[TimeQuantity]            = deriveCodec
given Codec[DepthType]               = deriveCodec
given originType: Codec[Origin.Type] = deriveCodec
given Codec[Origin]                  = deriveCodec
given Codec[DetectedEvent]           = deriveCodec
