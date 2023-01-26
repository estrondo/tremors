package graboid.protocol

import io.bullet.borer.Codec
import io.bullet.borer.derivation.MapBasedCodecs.deriveCodec
import java.time.ZonedDateTime
import cbor.core.given

object PublisherDescriptor:

  given Codec[PublisherDescriptor] = deriveCodec

case class PublisherDescriptor(
    key: String,
    name: String,
    beginning: ZonedDateTime,
    ending: Option[ZonedDateTime],
    location: String,
    `type`: String
)
