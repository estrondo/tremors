package tremors.graboid

import zio.Task
import tremors.graboid.command.*

trait CommandExecutor:

  def apply(command: CommandDescriptor): Task[CommandExecution]

object CommandExecutor

private[graboid] class CommandExecutorImpl extends CommandExecutor:

  override def apply(command: CommandDescriptor): Task[CommandExecution] = ???