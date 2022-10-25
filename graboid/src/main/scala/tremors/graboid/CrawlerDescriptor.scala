package tremors.graboid

import io.bullet.borer.Codec
import io.bullet.borer.derivation.MapBasedCodecs.deriveCodec
import tremors.graboid.BorerCodecs.given

import java.time.Duration
import java.time.ZonedDateTime

object CrawlerDescriptor:

  given Codec[CrawlerDescriptor] = deriveCodec

case class CrawlerDescriptor(
    name: String,
    `type`: String,
    source: String,
    windowDuration: Duration,
    starting: ZonedDateTime
)
