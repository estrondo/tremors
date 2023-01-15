package graboid.command

import graboid.EventPublisherManager
import graboid.protocol.GraboidCommandResult
import graboid.protocol.GraboidCommandResult.Status
import graboid.protocol.RemoveEventPublisher
import zio.Task

trait RemoveEventPublisherExecutor extends GraboidCommandExecutor[RemoveEventPublisher]

class RemoveEventPublisherExecutorImpl(
    eventPublisherManager: EventPublisherManager
) extends RemoveEventPublisherExecutor:

  override def execute(command: RemoveEventPublisher): Task[Status] =
    for result <- eventPublisherManager.remove(command.publisherKey)
    yield GraboidCommandResult.Ok(s"EventPublisher(${result.map(_.key).getOrElse("")})")
