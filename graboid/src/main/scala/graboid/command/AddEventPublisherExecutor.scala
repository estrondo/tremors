package graboid.command

import graboid.protocol.AddEventPublisher
import graboid.EventPublisherManager
import graboid.EventPublisher
import io.github.arainko.ducktape.into
import io.github.arainko.ducktape.Field
import java.net.URL
import graboid.Crawler
import zio.{ZIO, UIO, Task}
import graboid.protocol.EventPublisherDescriptor
import graboid.protocol.GraboidCommandResult
import zio.ZIOAspect

trait AddEventPublisherExecutor extends GraboidCommandExecutor[AddEventPublisher]

class AddEventPublisherExecutorImpl(
    eventPublisherManager: EventPublisherManager
) extends AddEventPublisherExecutor:

  override def execute(command: AddEventPublisher): Task[GraboidCommandResult.Status] =
    for
      eventPublisher <- ZIO.attempt(EventPublisher.from(command.descriptor))
      added          <- eventPublisherManager.add(eventPublisher)
    yield GraboidCommandResult.Ok(s"EventPublisher(${added.key})")
