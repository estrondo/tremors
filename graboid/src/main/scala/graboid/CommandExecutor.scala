package graboid

import com.softwaremill.macwire.wire
import graboid.command.DataCentreExecutor
import graboid.protocol.DataCentreCommand
import graboid.protocol.GraboidCommand
import zio.Task
import zio.ZIO

trait CommandExecutor:

  def apply(command: GraboidCommand): Task[GraboidCommand]

object CommandExecutor:

  def apply(dataCentreExecutor: DataCentreExecutor): Task[CommandExecutor] =
    ZIO.succeed(wire[Impl])

  private class Impl(dataCentreExecutor: DataCentreExecutor) extends CommandExecutor:

    override def apply(command: GraboidCommand): Task[GraboidCommand] =
      command match
        case command: DataCentreCommand => dataCentreExecutor(command)
        case _                          => ZIO.fail(GraboidException.Command(s"Invalid command: $command."))
