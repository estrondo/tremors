package graboid.protocol

import io.bullet.borer.Codec
import io.bullet.borer.derivation.MapBasedCodecs.deriveCodec
import java.time.ZonedDateTime
import cbor.core.given

object EventPublisherDescriptor:

  given Codec[EventPublisherDescriptor] = deriveCodec

case class EventPublisherDescriptor(
    key: String,
    name: String,
    beginning: ZonedDateTime,
    ending: Option[ZonedDateTime],
    location: String,
    `type`: String
)
