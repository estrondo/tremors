package cbor.quakeml

import cbor.core.given
import io.bullet.borer.Codec
import io.bullet.borer.Decoder
import io.bullet.borer.Encoder
import io.bullet.borer.derivation.MapBasedCodecs.*
import quakeml.QuakeMLComment
import quakeml.QuakeMLCreationInfo
import quakeml.QuakeMLDetectedEvent
import quakeml.QuakeMLEvaluationMode
import quakeml.QuakeMLEvaluationStatus
import quakeml.QuakeMLEvent
import quakeml.QuakeMLMagnitude
import quakeml.QuakeMLOrigin
import quakeml.QuakeMLOrigin.DepthType
import quakeml.QuakeMLRealQuantity
import quakeml.QuakeMLResourceReference
import quakeml.QuakeMLTimeQuantity

import java.time.Clock
import java.time.Instant
import java.time.ZonedDateTime
import java.time.temporal.ChronoField
import scala.util.Try

private[cbor] val ZoneId = Clock.systemUTC().getZone()

given Codec[QuakeMLEvaluationMode]          = deriveCodec
given Codec[QuakeMLEvaluationStatus]        = deriveCodec
given Codec[QuakeMLRealQuantity]            = deriveCodec
given Codec[QuakeMLMagnitude]               = deriveCodec
given Codec[QuakeMLResourceReference]       = deriveCodec
given eventType: Codec[QuakeMLEvent.Type]   = deriveCodec
given Codec[QuakeMLEvent.TypeCertainty]     = deriveCodec
given Codec[QuakeMLEvent.DescriptionType]   = deriveCodec
given Codec[QuakeMLEvent.Description]       = deriveCodec
given Codec[QuakeMLCreationInfo]            = deriveCodec
given Codec[QuakeMLComment]                 = deriveCodec
given Codec[QuakeMLEvent]                   = deriveCodec
given Codec[QuakeMLTimeQuantity]            = deriveCodec
given Codec[DepthType]                      = deriveCodec
given originType: Codec[QuakeMLOrigin.Type] = deriveCodec
given Codec[QuakeMLOrigin]                  = deriveCodec
given Codec[QuakeMLDetectedEvent]           = deriveCodec
