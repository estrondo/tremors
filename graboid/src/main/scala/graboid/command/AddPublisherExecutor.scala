package graboid.command

import graboid.Crawler
import graboid.Publisher
import graboid.PublisherManager
import graboid.protocol.AddPublisher
import graboid.protocol.GraboidCommandResult
import graboid.protocol.PublisherDescriptor
import io.github.arainko.ducktape.Field
import io.github.arainko.ducktape.into
import java.net.URL
import zio.Task
import zio.UIO
import zio.ZIO
import zio.ZIOAspect

trait AddPublisherExecutor extends GraboidCommandExecutor[AddPublisher]

class AddPublisherExecutorImpl(
    publisherManager: PublisherManager
) extends AddPublisherExecutor:

  override def execute(command: AddPublisher): Task[GraboidCommandResult.Status] =
    for
      publisher <- ZIO.attempt(Publisher.from(command.descriptor))
      added     <- publisherManager.add(publisher)
    yield GraboidCommandResult.ok(s"Publisher added.", "publisherKey" -> command.descriptor.key)
