package graboid

import graboid.CrawlerManager.CrawlerReport
import graboid.config.GraboidConfig
import zio.ExitCode
import zio.Scope
import zio.Task
import zio.UIO
import zio.ZIO
import zio.ZIOAppArgs
import zio.ZIOAppDefault
import zio.config.ConfigSource
import zio.stream.ZSink

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
      httpModule     <- HttpModule(graboidConfig.httpClient)
      kafkaModule    <- KafkaModule()
      databaseModule <- DatabaseModule(graboidConfig)
      crawlerModule  <-
        CrawlerModule(graboidConfig.crawlerManager, httpModule, kafkaModule, databaseModule)
      _              <- crawlerModule.runManager().run(ZSink.drain)
      _              <- ZIO.logInfo(
                          s"Graboid [${BuildInfo.version}] is starting, please keep yourself away from them 🪱."
                        )
    yield ExitCode.success
