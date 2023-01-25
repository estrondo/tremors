package graboid.command

import graboid.Crawler
import graboid.EventPublisher
import graboid.EventPublisherManager
import graboid.protocol.AddEventPublisher
import graboid.protocol.EventPublisherDescriptor
import graboid.protocol.GraboidCommandResult
import io.github.arainko.ducktape.Field
import io.github.arainko.ducktape.into
import zio.Task
import zio.UIO
import zio.ZIO
import zio.ZIOAspect

import java.net.URL

trait AddEventPublisherExecutor extends GraboidCommandExecutor[AddEventPublisher]

class AddEventPublisherExecutorImpl(
    eventPublisherManager: EventPublisherManager
) extends AddEventPublisherExecutor:

  override def execute(command: AddEventPublisher): Task[GraboidCommandResult.Status] =
    for
      eventPublisher <- ZIO.attempt(EventPublisher.from(command.descriptor))
      added          <- eventPublisherManager.add(eventPublisher)
    yield GraboidCommandResult.Ok(s"EventPublisher(${added.key})")
