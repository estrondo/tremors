package graboid.protocol

import borercodec.given
import io.bullet.borer.Codec
import io.bullet.borer.derivation.MapBasedCodecs.deriveAllCodecs
import io.bullet.borer.derivation.MapBasedCodecs.deriveCodec

import java.time.Duration
import java.time.ZonedDateTime

object GraboidCommand:

  given Codec[GraboidCommand] = deriveAllCodecs

sealed trait GraboidCommand

case class AddCrawler(descriptor: CrawlerDescriptor) extends GraboidCommand

case class RemoveCrawler(name: String) extends GraboidCommand

case class UpdateCrawler(name: String, descriptor: CrawlerDescriptor, shouldRunNow: Boolean)
    extends GraboidCommand

case class GraboidCommandExecution(milliseconds: Long, command: GraboidCommand)

case class RunCrawler(name: String) extends GraboidCommand

case object RunAll extends GraboidCommand
