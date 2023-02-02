package graboid

import com.softwaremill.macwire.wire
import com.softwaremill.macwire.wireWith
import graboid.command.AddPublisherExecutorImpl
import graboid.command.RemovePublisherExecutor
import graboid.command.RemovePublisherExecutorImpl
import graboid.command.UpdatePublisherExecutorImpl
import graboid.kafka.GraboidCommandTopic
import zio.Task
import zio.ZIO
import zio.UIO

import graboid.CoreModule
import zio.FiberRef
import zio.Fiber
trait CommandModule:

  def commandListener: CommandListener

  def commandExecutor: CommandExecutor

  def run(): Task[Unit]

  def start(): UIO[Fiber.Runtime[Throwable, Unit]] =
    run().fork

object CommandModule:

  def apply(coreModule: CoreModule, kafkaModule: KafkaModule): Task[CommandModule] = ZIO.attempt {
    wire[Impl]
  }

  private class Impl(coreModule: CoreModule, kafkaModule: KafkaModule) extends CommandModule:

    private def publisherManager = coreModule.publisherManager

    val addPublisherExecutor = wire[AddPublisherExecutorImpl]

    val updatePublisherExecutor = wire[UpdatePublisherExecutorImpl]

    val removePublisherExecutor = wire[RemovePublisherExecutorImpl]

    override val commandExecutor: CommandExecutor = wireWith(CommandExecutor.apply)
    override val commandListener: CommandListener = wireWith(CommandListener.apply)

    val commandResultPublisher = CommandResultPublisher()

    def run(): Task[Unit] =
      for
        commandStream <- kafkaModule.subscribe(GraboidCommandTopic, commandListener, commandResultPublisher)
        _             <- commandStream.runDrain
      yield ()
