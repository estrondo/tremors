package tremors.graboid.command

import io.bullet.borer.Codec
import io.bullet.borer.derivation.MapBasedCodecs.deriveAllCodecs

sealed trait CommandDescriptor

case class AddCrawler(name: String) extends CommandDescriptor

case class RemoveCrawler(name: String) extends CommandDescriptor

case class UpdateCrawler(name: String) extends CommandDescriptor

case class CommandExecution(descriptor: CommandDescriptor)

given Codec[CommandDescriptor] = deriveAllCodecs
