package graboid.protocol

import cbor.core.given
import io.bullet.borer.Codec
import io.bullet.borer.derivation.MapBasedCodecs.deriveCodec

import java.time.Duration
import java.time.ZonedDateTime

object CrawlerDescriptor:

  given Codec[CrawlerDescriptor] = deriveCodec

case class CrawlerDescriptor(
    key: String,
    name: String,
    `type`: String,
    source: String,
    windowDuration: Duration,
    starting: ZonedDateTime
)

object UpdateCrawlerDescriptor:

  given Codec[UpdateCrawlerDescriptor] = deriveCodec

case class UpdateCrawlerDescriptor(
    name: Option[String],
    `type`: Option[String],
    source: Option[String],
    windowDuration: Option[Duration],
    starting: Option[ZonedDateTime]
)
