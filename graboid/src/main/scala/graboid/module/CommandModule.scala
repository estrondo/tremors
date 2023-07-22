package graboid.module

import com.softwaremill.macwire.Module
import graboid.CommandExecutor
import graboid.command.DataCentreExecutor
import zio.Task

@Module
trait CommandModule:

  def commandExecutor: CommandExecutor

object CommandModule:

  def apply(managerModule: ManagerModule): Task[CommandModule] =
    for
      dataCentreExecutor <- DataCentreExecutor(managerModule.dataCentreManager)
      commandExecutor    <- CommandExecutor(dataCentreExecutor)
    yield Impl(commandExecutor)

  private class Impl(
      val commandExecutor: CommandExecutor
  ) extends CommandModule
