package graboid.module

import graboid.command.CommandListener
import graboid.protocol.GraboidCommand
import graboid.protocol.GraboidCommandResult
import tremors.generator.KeyLength
import tremors.zio.kafka.KConPro
import tremors.zio.kafka.KReader
import tremors.zio.kafka.KWriter
import tremors.zio.kafka.cbor.Borer
import zio.Task
import zio.stream.ZStream

trait ListenerModule:

  def commandResultStream: ZStream[Any, Throwable, GraboidCommandResult]

object ListenerModule:

  def apply(commandModule: CommandModule, kafkaModule: KafkaModule): Task[ListenerModule] =
    for commandListener <- CommandListener(commandModule.commandExecutor)
    yield Impl(commandListener, kafkaModule)

  private class Impl(commandListener: CommandListener, kafkaModule: KafkaModule) extends ListenerModule:

    override val commandResultStream: ZStream[Any, Nothing, GraboidCommandResult] =
      given KReader[GraboidCommand]       = Borer.readerFor[GraboidCommand]
      given KWriter[GraboidCommandResult] = Borer.writerFor[GraboidCommandResult]
      kafkaModule.router.subscribe[GraboidCommand, GraboidCommandResult](
        KConPro.AutoGeneratedKey(
          subscriptionTopic = "graboid.command",
          productTopic = "graboid.command-result",
          keyLength = KeyLength.Medium,
          mapper = (_, command) => ZStream.fromZIO(commandListener(command))
        )
      )
