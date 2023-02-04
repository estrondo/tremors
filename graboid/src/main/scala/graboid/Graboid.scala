package graboid

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

import graboid.CoreModule
object Graboid extends ZIOAppDefault:

  override def run: ZIO[ZIOAppArgs & Scope, Any, Any] =
    for
      logger        <- LoggerModule().logger
      graboidConfig <- ConfigModule().config.orDie.provideSomeLayer(logger)
      exitCode      <- application(graboidConfig).orDie.provideLayer(logger)
    yield exitCode

  def application(graboidConfig: GraboidConfig): Task[ExitCode] =
    for
      arangoModule     <- ArangoModule(graboidConfig.arango)
      kafkaModule      <- KafkaModule(graboidConfig.kafka)
      httpModule       <- HttpModule(graboidConfig.httpClient)
      repositoryModule <- RepositoryModule(arangoModule)
      coreModule       <- CoreModule(graboidConfig, repositoryModule, kafkaModule, httpModule)

      crawlerExecutorModule <-
        CrawlerExecutorModule(graboidConfig.crawlerExecutor, repositoryModule, coreModule, httpModule)

      commandModule        <- CommandModule(coreModule, kafkaModule, crawlerExecutorModule)
      commandStreamFiber   <- commandModule.start()
      crawlerExecutorFiber <- crawlerExecutorModule.start()

      _ <- ZIO.logInfo(
             s"Graboid [${BuildInfo.version}] is starting, please keep yourself away from them 🪱."
           )

      _ <- commandStreamFiber.join
      _ <- crawlerExecutorFiber.join
    yield ExitCode.success
