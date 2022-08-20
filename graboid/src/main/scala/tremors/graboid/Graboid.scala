package tremors.graboid

import zio.Console
import zio.ExitCode
import zio.Scope
import zio.UIO
import zio.ZIO
import zio.ZIOAppArgs
import zio.ZIOAppDefault
import zio._
import zio.config.ConfigSource

object Graboid extends ZIOAppDefault {

  override def run: ZIO[Any & (ZIOAppArgs & Scope), Any, Any] =
    for
      logger       <- LoggerModule().logger
      configSource <- ConfigModule().configSource
      exitCode     <- application(configSource).provideLayer(logger)
    yield exitCode

  def application(configSource: ConfigSource): UIO[ExitCode] =
    for _ <- ZIO.logInfo("Graboid is starting, please keep yourself away from them 🪱.")
    yield ExitCode.success
}
