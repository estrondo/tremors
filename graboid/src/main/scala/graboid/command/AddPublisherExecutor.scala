package graboid.command

import graboid.Crawler
import graboid.Publisher
import graboid.PublisherManager
import graboid.protocol.AddPublisher
import graboid.protocol.PublisherDescriptor
import graboid.protocol.GraboidCommandResult
import io.github.arainko.ducktape.Field
import io.github.arainko.ducktape.into
import zio.Task
import zio.UIO
import zio.ZIO
import zio.ZIOAspect

import java.net.URL

trait AddPublisherExecutor extends GraboidCommandExecutor[AddPublisher]

class AddPublisherExecutorImpl(
    publisherManager: PublisherManager
) extends AddPublisherExecutor:

  override def execute(command: AddPublisher): Task[GraboidCommandResult.Status] =
    for
      publisher <- ZIO.attempt(Publisher.from(command.descriptor))
      added     <- publisherManager.add(publisher)
    yield GraboidCommandResult.Ok(s"Publisher(${added.key})")
