package graboid

import com.softwaremill.macwire.wire
import graboid.command.CrawlingCommandExecutor
import graboid.command.DataCentreCommandExecutor
import graboid.protocol.CrawlingCommand
import graboid.protocol.DataCentreCommand
import graboid.protocol.GraboidCommand
import zio.Task
import zio.ZIO

trait CommandExecutor:

  def apply(command: GraboidCommand): Task[GraboidCommand]

object CommandExecutor:

  def apply(
      dataCentreCommandExecutor: DataCentreCommandExecutor,
      crawlingCommandExecutor: CrawlingCommandExecutor
  ): Task[CommandExecutor] =
    ZIO.succeed(wire[Impl])

  private class Impl(
      dataCentreCommandExecutor: DataCentreCommandExecutor,
      crawlingCommandExecutor: CrawlingCommandExecutor
  ) extends CommandExecutor:

    override def apply(command: GraboidCommand): Task[GraboidCommand] =
      command match
        case command: DataCentreCommand => dataCentreCommandExecutor(command)
        case command: CrawlingCommand   => crawlingCommandExecutor(command)
        case _                          => ZIO.fail(GraboidException.Command(s"Invalid command: $command."))
