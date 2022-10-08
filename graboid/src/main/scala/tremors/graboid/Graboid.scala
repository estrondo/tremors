package tremors.graboid

import zio.ExitCode
import zio.Scope
import zio.UIO
import zio.ZIO
import zio.ZIOAppArgs
import zio.ZIOAppDefault
import zio.config.ConfigSource

object Graboid extends ZIOAppDefault:

  override def run: ZIO[ZIOAppArgs & Scope, Any, Any] =
    for
      logger       <- LoggerModule().logger
      configSource <- ConfigModule().configSource.provideLayer(logger)
      exitCode     <- application(configSource).provideLayer(logger)
    yield exitCode

  def application(configSource: ConfigSource): UIO[ExitCode] =
    for _ <-
        ZIO.logInfo(
          s"Graboid [${BuildInfo.version}] is starting, please keep yourself away from them 🪱."
        )
    yield ExitCode.success
