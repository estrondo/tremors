package tremors.graboid

import zio.Console
import zio.ExitCode
import zio.Scope
import zio.UIO
import zio.ZIO
import zio.ZIOAppArgs
import zio.ZIOAppDefault

object Graboid extends ZIOAppDefault {

  override def run: ZIO[Any & (ZIOAppArgs & Scope), Any, Any] =
    for
      logger   <- LoggerModule().logger
      exitCode <- start().provideLayer(logger)
    yield exitCode

  def start(): UIO[ExitCode] =
    for _ <- ZIO.logInfo(
               "Graboid is starting, please keep yourself away from them 🪱."
             )
    yield ExitCode.success
}
