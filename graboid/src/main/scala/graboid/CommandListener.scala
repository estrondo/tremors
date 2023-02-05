package graboid

import com.softwaremill.macwire.wire
import zkafka.KafkaSubscriber
import graboid.protocol.GraboidCommand
import graboid.protocol.GraboidCommandResult
import zio.Task
import zio.ZIO

trait CommandListener extends KafkaSubscriber[GraboidCommand, GraboidCommandResult]

object CommandListener:

  def apply(executor: CommandExecutor): CommandListener =
    wire[Impl]

  private class Impl(
      executor: CommandExecutor
  ) extends CommandListener:

    def accept(key: String, command: GraboidCommand): Task[Option[GraboidCommandResult]] =
      for
        _      <- ZIO.logDebug(s"A new command with key=$key has just been received: id=${command.id}.")
        result <- executor(command)
      yield Some(result)
