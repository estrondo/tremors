package tremors.graboid

import zio.ExitCode
import zio.Scope
import zio.Task
import zio.ZIO
import zio.ZIOAppArgs
import zio.ZIOAppDefault
import zio.config.ConfigSource

object Graboid extends ZIOAppDefault:

  override def run: ZIO[ZIOAppArgs & Scope, Any, Any] =
    for
      logger        <- LoggerModule().logger
      graboidConfig <- ConfigModule().config.provideLayer(logger)
      exitCode      <- application(graboidConfig).provideLayer(logger)
    yield exitCode

  def application(graboidConfig: GraboidConfig): Task[ExitCode] =
    for
      crawlerModule <- CrawlerModule(graboidConfig.crawlerManager)
      _             <- crawlerModule.runManager()
      _             <- ZIO.logInfo(
                         s"Graboid [${BuildInfo.version}] is starting, please keep yourself away from them 🪱."
                       )
    yield ExitCode.success
