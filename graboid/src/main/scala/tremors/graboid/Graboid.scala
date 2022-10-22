package tremors.graboid

import zio.ExitCode
import zio.Scope
import zio.Task
import zio.ZIO
import zio.UIO
import zio.ZIOAppArgs
import zio.ZIOAppDefault
import zio.config.ConfigSource
import tremors.graboid.config.GraboidConfig
import zio.stream.ZSink
import tremors.graboid.CrawlerManager.CrawlerReport

object Graboid extends ZIOAppDefault:

  override def run: ZIO[ZIOAppArgs & Scope, Any, Any] =
    for
      logger        <- LoggerModule().logger
      graboidConfig <- ConfigModule().config.provideLayer(logger)
      exitCode      <- application(graboidConfig)
                         .orDieWith(x => {
                           x.printStackTrace()
                           x
                         })
                         .provideLayer(logger)
    yield exitCode

  def application(graboidConfig: GraboidConfig): Task[ExitCode] =
    for
      httpModule     <- HttpModule()
      kafkaModule    <- KafkaModule()
      databaseModule <- DatabaseModule(graboidConfig)
      crawlerModule  <-
        CrawlerModule(graboidConfig.crawlerManager, httpModule, kafkaModule, databaseModule)
      _              <- crawlerModule.runManager().run(ZSink.drain)
      _              <- ZIO.logInfo(
                          s"Graboid [${BuildInfo.version}] is starting, please keep yourself away from them 🪱."
                        )
    yield ExitCode.success
