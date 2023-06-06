package graboid.command

import graboid.Publisher
import graboid.PublisherManager
import graboid.protocol.AddPublisher
import graboid.protocol.GraboidCommandResult
import zio.Task
import zio.ZIO

trait AddPublisherExecutor extends GraboidCommandExecutor[AddPublisher]

class AddPublisherExecutorImpl(
    publisherManager: PublisherManager
) extends AddPublisherExecutor:

  override def execute(command: AddPublisher): Task[GraboidCommandResult.Status] =
    for
      publisher <- ZIO.attempt(Publisher.from(command.descriptor))
      added     <- publisherManager.add(publisher)
    yield GraboidCommandResult.ok(s"Publisher added.", "publisherKey" -> command.descriptor.key)
