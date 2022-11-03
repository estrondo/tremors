package graboid.protocol

import borercodec.given
import io.bullet.borer.Codec
import io.bullet.borer.derivation.MapBasedCodecs.deriveAllCodecs
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

object CommandDescriptor:

  given Codec[CommandDescriptor] = deriveAllCodecs

sealed trait CommandDescriptor

case class AddCrawler(descriptor: CrawlerDescriptor) extends CommandDescriptor

case class RemoveCrawler(name: String) extends CommandDescriptor

case class UpdateCrawler(name: String, descriptor: CrawlerDescriptor, shouldRunNow: Boolean)
    extends CommandDescriptor

case class CommandExecution(milliseconds: Long, descriptor: CommandDescriptor)

case class RunCrawler(name: String) extends CommandDescriptor

case object RunAll extends CommandDescriptor
