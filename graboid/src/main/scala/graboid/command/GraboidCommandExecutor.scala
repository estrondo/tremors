package graboid.command

import GraboidCommandExecutor.CommandAnnotation
import graboid.protocol.GraboidCommand
import graboid.protocol.GraboidCommandResult
import zio.Cause
import zio.Task
import zio.UIO
import zio.ZIO
import zio.logging.LogAnnotation

object GraboidCommandExecutor:

  val CommandAnnotation =
    LogAnnotation[GraboidCommand](
      "graboid-command",
      (_, x) => x,
      x => s"${x.getClass().getSimpleName()}: id=${x.id}"
    )

trait GraboidCommandExecutor[T <: GraboidCommand]:

  def apply(command: T): UIO[GraboidCommandResult] =
    val startTime = System.currentTimeMillis()
    (for status <- execute(command)
    yield GraboidCommandResult(command.id, System.currentTimeMillis() - startTime, status))
      .catchAll(handleError(command, startTime)) @@ CommandAnnotation(
      command
    )

  def execute(command: T): Task[GraboidCommandResult.Status]

  protected def handleError(command: T, startTime: Long)(
      error: Throwable
  ): UIO[GraboidCommandResult] =
    ZIO.logErrorCause(
      "A error has happend during adding of an Publisher!",
      Cause.die(error)
    ) as GraboidCommandResult(
      command.id,
      System.currentTimeMillis() - startTime,
      GraboidCommandResult.failed("An error ocurred!", error)
    )
