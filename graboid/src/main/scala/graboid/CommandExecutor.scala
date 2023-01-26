package graboid

import com.softwaremill.macwire.wire
import graboid.PublisherManager
import graboid.command.AddPublisherExecutor
import graboid.command.RemovePublisherExecutor
import graboid.command.UpdatePublisherExecutor
import graboid.protocol.AddPublisher
import graboid.protocol.PublisherDescriptor
import graboid.protocol.GraboidCommand
import graboid.protocol.GraboidCommandResult
import graboid.protocol.RemovePublisher
import graboid.protocol.UpdatePublisher
import zio.Task
import zio.UIO

trait CommandExecutor:

  def apply(command: GraboidCommand): UIO[GraboidCommandResult]

object CommandExecutor:

  def apply(
      addPublisherExecutor: AddPublisherExecutor,
      removePublisherExecutor: RemovePublisherExecutor,
      updatePublisherExecutor: UpdatePublisherExecutor
  ): CommandExecutor =
    wire[CommandExecutorImpl]

  private class CommandExecutorImpl(
      addPublisherExecutor: AddPublisherExecutor,
      removePublisherExecutor: RemovePublisherExecutor,
      updatePublisherExecutor: UpdatePublisherExecutor
  ) extends CommandExecutor:

    override def apply(command: GraboidCommand): UIO[GraboidCommandResult] = command match
      case cmd: AddPublisher    => addPublisherExecutor(cmd)
      case cmd: RemovePublisher => removePublisherExecutor(cmd)
      case cmd: UpdatePublisher => updatePublisherExecutor(cmd)
