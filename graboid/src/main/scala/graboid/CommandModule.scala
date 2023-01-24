package graboid

import com.softwaremill.macwire.wire
import com.softwaremill.macwire.wireWith
import graboid.command.AddEventPublisherExecutorImpl
import graboid.command.RemoveEventPublisherExecutor
import graboid.command.RemoveEventPublisherExecutorImpl
import graboid.command.UpdateEventPublisherExecutorImpl
import graboid.kafka.GraboidCommandTopic
import zio.Task
import zio.ZIO

trait CommandModule:

  val commandListener: CommandListener
  val commandExecutor: CommandExecutor

  def run(): Task[Unit]

object CommandModule:

  def apply(coreModule: CoreModule, kafkaModule: KafkaModule): Task[CommandModule] = ZIO.attempt {
    wire[CommandModuleImpl]
  }

  private class CommandModuleImpl(coreModule: CoreModule, kafkaModule: KafkaModule) extends CommandModule:

    def eventPublisherManager = coreModule.eventPublisherManager

    val addEventPublisherExecutor = wire[AddEventPublisherExecutorImpl]

    val updateEventPublisherExecutor = wire[UpdateEventPublisherExecutorImpl]

    val removeEventPublisherExecutor = wire[RemoveEventPublisherExecutorImpl]

    override val commandExecutor: CommandExecutor = wireWith(CommandExecutor.apply)
    override val commandListener: CommandListener = wireWith(CommandListener.apply)

    val commandResultPublisher = CommandResultPublisher()

    def run(): Task[Unit] =
      for
        commandStream <- kafkaModule.kafkaManager
                           .subscribe(GraboidCommandTopic, commandListener, commandResultPublisher)
        _             <- commandStream.runDrain
      yield ()
