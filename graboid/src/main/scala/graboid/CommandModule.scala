package graboid

import com.softwaremill.macwire.wire
import com.softwaremill.macwire.wireWith
import graboid.command.AddPublisherExecutorImpl
import graboid.command.RemovePublisherExecutor
import graboid.command.RemovePublisherExecutorImpl
import graboid.command.RunAllPublishersExecutorImpl
import graboid.command.RunPublisherExecutorImpl
import graboid.command.UpdatePublisherExecutorImpl
import graboid.kafka.GraboidCommandTopic
import zio.Fiber
import zio.Task
import zio.UIO
import zio.ZIO
trait CommandModule:

  def commandListener: CommandListener

  def commandExecutor: CommandExecutor

  def run(): Task[Unit]

  def start(): UIO[Fiber.Runtime[Throwable, Unit]] =
    run().fork

object CommandModule:

  def apply(
      coreModule: CoreModule,
      kafkaModule: KafkaModule,
      crawlerExecutorModule: CrawlerExecutorModule
  ): Task[CommandModule] = ZIO.attempt {
    wire[Impl]
  }

  private class Impl(coreModule: CoreModule, kafkaModule: KafkaModule, crawlerExecutorModule: CrawlerExecutorModule)
      extends CommandModule:

    private def publisherManager = coreModule.publisherManager

    private def crawlerExecutor = crawlerExecutorModule.crawlerExecutor

    val addPublisherExecutor     = wire[AddPublisherExecutorImpl]
    val updatePublisherExecutor  = wire[UpdatePublisherExecutorImpl]
    val removePublisherExecutor  = wire[RemovePublisherExecutorImpl]
    val runAllPublishersExecutor = wire[RunAllPublishersExecutorImpl]
    val runPublisherExecutor     = wire[RunPublisherExecutorImpl]

    override val commandExecutor: CommandExecutor = wireWith(CommandExecutor.apply)
    override val commandListener: CommandListener = wireWith(CommandListener.apply)

    val commandResultPublisher = CommandResultPublisher()

    def run(): Task[Unit] =
      for
        commandStream <- kafkaModule.subscribe(GraboidCommandTopic, commandListener, commandResultPublisher)
        _             <- commandStream.runDrain
      yield ()
