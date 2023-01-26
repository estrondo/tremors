package graboid.command

import graboid.PublisherManager
import graboid.protocol.GraboidCommandResult
import graboid.protocol.GraboidCommandResult.Status
import graboid.protocol.RemovePublisher
import zio.Task

trait RemovePublisherExecutor extends GraboidCommandExecutor[RemovePublisher]

class RemovePublisherExecutorImpl(
    publisherManager: PublisherManager
) extends RemovePublisherExecutor:

  override def execute(command: RemovePublisher): Task[Status] =
    for result <- publisherManager.remove(command.publisherKey)
    yield GraboidCommandResult.Ok(s"Publisher(${result.map(_.key).getOrElse("")})")
