package graboid.command

import graboid.Publisher
import graboid.PublisherManager
import graboid.protocol.GraboidCommandResult
import graboid.protocol.GraboidCommandResult.Status
import graboid.protocol.RemovePublisher
import graboid.protocol.UpdatePublisher
import zio.Task

trait UpdatePublisherExecutor extends GraboidCommandExecutor[UpdatePublisher]

class UpdatePublisherExecutorImpl(
    manager: PublisherManager
) extends UpdatePublisherExecutor:

  override def execute(command: UpdatePublisher): Task[Status] =
    for updated <- manager
                     .update(command.descriptor.key, Publisher.updateFrom(command.descriptor))
    yield GraboidCommandResult.ok("Publisher has been updated.", "publisherKey" -> command.descriptor.key)
