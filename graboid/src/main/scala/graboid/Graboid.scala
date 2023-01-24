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

object Graboid extends ZIOAppDefault:

  override def run: ZIO[ZIOAppArgs & Scope, Any, Any] =
    for
      logger        <- LoggerModule().logger
      graboidConfig <- ConfigModule().config.orDie.provideLayer(logger)
      exitCode      <- application(graboidConfig).orDie.provideLayer(logger)
    yield exitCode

  def application(graboidConfig: GraboidConfig): Task[ExitCode] =
    for
      arangoModule  <- ArangoModule(graboidConfig.arango)
      coreModule    <- CoreModule(graboidConfig, arangoModule)
      kafkaModule   <- KafkaModule(graboidConfig.kafka)
      httpModule    <- HttpModule(graboidConfig.httpClient)
      commandModule <- CommandModule(coreModule, kafkaModule)
      fiber1        <- commandModule.run().fork
      _             <- ZIO.logInfo(
                         s"Graboid [${BuildInfo.version}] is starting, please keep yourself away from them 🪱."
                       )
      _             <- fiber1.join
    yield ExitCode.success
