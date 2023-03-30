package graboid

import com.softwaremill.macwire.wire
import graboid.PublisherManager
import graboid.command.AddPublisherExecutor
import graboid.command.RemovePublisherExecutor
import graboid.command.RunAllPublishersExecutor
import graboid.command.RunPublisherExecutor
import graboid.command.UpdatePublisherExecutor
import graboid.protocol.AddPublisher
import graboid.protocol.GraboidCommand
import graboid.protocol.GraboidCommandResult
import graboid.protocol.PublisherDescriptor
import graboid.protocol.RemovePublisher
import graboid.protocol.RunAllPublishers
import graboid.protocol.RunPublisher
import graboid.protocol.UpdatePublisher
import zio.Task
import zio.UIO

trait CommandExecutor:

  def apply(command: GraboidCommand): UIO[GraboidCommandResult]

object CommandExecutor:

  def apply(
      addPublisherExecutor: AddPublisherExecutor,
      removePublisherExecutor: RemovePublisherExecutor,
      updatePublisherExecutor: UpdatePublisherExecutor,
      runAllPublishersExecutor: RunAllPublishersExecutor,
      runPublisherExecutor: RunPublisherExecutor
  ): CommandExecutor =
    wire[Impl]

  private class Impl(
      addPublisherExecutor: AddPublisherExecutor,
      removePublisherExecutor: RemovePublisherExecutor,
      updatePublisherExecutor: UpdatePublisherExecutor,
      runAllPublishersExecutor: RunAllPublishersExecutor,
      runPublisherExecutor: RunPublisherExecutor
  ) extends CommandExecutor:

    override def apply(command: GraboidCommand): UIO[GraboidCommandResult] = command match
      case cmd: AddPublisher     => addPublisherExecutor(cmd)
      case cmd: RemovePublisher  => removePublisherExecutor(cmd)
      case cmd: UpdatePublisher  => updatePublisherExecutor(cmd)
      case cmd: RunAllPublishers => runAllPublishersExecutor(cmd)
      case cmd: RunPublisher     => runPublisherExecutor(cmd)
