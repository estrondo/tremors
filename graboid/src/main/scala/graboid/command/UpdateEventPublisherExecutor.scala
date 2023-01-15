package graboid.command

import graboid.EventPublisher
import graboid.EventPublisherManager
import graboid.protocol.GraboidCommandResult
import graboid.protocol.GraboidCommandResult.Status
import graboid.protocol.RemoveEventPublisher
import graboid.protocol.UpdateEventPublisher
import zio.Task

trait UpdateEventPublisherExecutor extends GraboidCommandExecutor[UpdateEventPublisher]

class UpdateEventPublisherExecutorImpl(
    manager: EventPublisherManager
) extends UpdateEventPublisherExecutor:

  override def execute(command: UpdateEventPublisher): Task[Status] =
    for updated <- manager
                     .update(command.descriptor.key, EventPublisher.updateFrom(command.descriptor))
    yield GraboidCommandResult.Ok(s"EventPublisher(${updated.map(_.key).getOrElse("")})")
