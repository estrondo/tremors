package tremors.graboid.command

import io.bullet.borer.Codec
import io.bullet.borer.derivation.MapBasedCodecs.deriveAllCodecs
import tremors.graboid.CrawlerDescriptor

sealed trait CommandDescriptor

case class AddCrawler(descriptor: CrawlerDescriptor) extends CommandDescriptor

case class RemoveCrawler(name: String) extends CommandDescriptor

case class UpdateCrawler(name: String, descriptor: CrawlerDescriptor, shouldRunNow: Boolean)
    extends CommandDescriptor

case class CommandExecution(milliseconds: Long, descriptor: CommandDescriptor)

case class RunCrawler(name: String) extends CommandDescriptor

case object RunAll extends CommandDescriptor

given Codec[CommandDescriptor] = deriveAllCodecs
