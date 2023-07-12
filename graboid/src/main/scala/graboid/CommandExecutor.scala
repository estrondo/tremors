package graboid

import graboid.protocol.GraboidCommand
import zio.Task

trait CommandExecutor:

  def apply(command: GraboidCommand): Task[GraboidCommand]
