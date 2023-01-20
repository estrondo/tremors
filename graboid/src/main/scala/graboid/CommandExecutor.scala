package graboid

import com.softwaremill.macwire.wire
import graboid.EventPublisherManager
import graboid.command.AddEventPublisherExecutor
import graboid.command.RemoveEventPublisherExecutor
import graboid.command.UpdateEventPublisherExecutor
import graboid.protocol.AddEventPublisher
import graboid.protocol.EventPublisherDescriptor
import graboid.protocol.GraboidCommand
import graboid.protocol.GraboidCommandResult
import graboid.protocol.RemoveEventPublisher
import graboid.protocol.UpdateEventPublisher
import zio.Task
import zio.UIO

trait CommandExecutor:

  def apply(command: GraboidCommand): UIO[GraboidCommandResult]

object CommandExecutor:

  def apply(
      addEventPublisherExecutor: AddEventPublisherExecutor,
      removeEventPublisherExecutor: RemoveEventPublisherExecutor,
      updateEventPublisherExecutor: UpdateEventPublisherExecutor
  ): CommandExecutor =
    wire[CommandExecutorImpl]

  private class CommandExecutorImpl(
      addEventPublisherExecutor: AddEventPublisherExecutor,
      removeEventPublisherExecutor: RemoveEventPublisherExecutor,
      updateEventPublisherExecutor: UpdateEventPublisherExecutor
  ) extends CommandExecutor:

    override def apply(command: GraboidCommand): UIO[GraboidCommandResult] = command match
      case cmd: AddEventPublisher    => addEventPublisherExecutor(cmd)
      case cmd: RemoveEventPublisher => removeEventPublisherExecutor(cmd)
      case cmd: UpdateEventPublisher => updateEventPublisherExecutor(cmd)
