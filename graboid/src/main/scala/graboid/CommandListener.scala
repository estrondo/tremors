package graboid

import com.softwaremill.macwire.wire
import graboid.protocol.GraboidCommand
import graboid.protocol.GraboidCommandFailure
import graboid.protocol.GraboidCommandResult
import graboid.protocol.GraboidCommandSuccess
import zio.Cause
import zio.Exit
import zio.Task
import zio.UIO
import zio.ZIO

trait CommandListener:

  def apply(command: GraboidCommand): Task[GraboidCommandResult]

object CommandListener:

  def apply(executor: CommandExecutor): UIO[CommandListener] =
    ZIO.succeed(wire[Impl])

  private class Impl(executor: CommandExecutor) extends CommandListener:

    override def apply(command: GraboidCommand): Task[GraboidCommandResult] =
      for
        _        <- ZIO.logDebug(s"A command ${command.commandId} has been received.")
        exit     <- executor(command).exit
        response <- exit match
                      case Exit.Success(command)                  =>
                        ZIO.logDebug(s"Command ${command.commandId} has been proceed.") *> ZIO.succeed(
                          GraboidCommandSuccess(command.commandId)
                        )
                      case Exit.Failure(cause @ Cause.Fail(_, _)) =>
                        reportError(s"Command ${command.commandId} has been failed!", cause, command)
                      case Exit.Failure(cause @ Cause.Die(_, _))  =>
                        reportError(s"Command ${command.commandId} has been crashed!", cause, command)
                      case Exit.Failure(cause)                    =>
                        reportError(s"Command ${command.commandId} has gone wrong!", cause, command)
      yield response

    private inline def reportError(
        inline message: String,
        inline cause: Cause[Throwable],
        inline command: GraboidCommand
    ): UIO[GraboidCommandResult] =
      ZIO.logWarningCause(message, cause) *> ZIO.succeed(
        GraboidCommandFailure(command.commandId, cause.squash.getMessage)
      )
