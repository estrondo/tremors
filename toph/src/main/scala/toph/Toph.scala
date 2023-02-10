package toph

import toph.module.ConfigModule
import toph.module.CoreModule
import toph.module.FarangoModule
import toph.module.KafkaModule
import toph.module.RepositoryModule
import zio.ExitCode
import zio.Runtime
import zio.Schedule
import zio.Scope
import zio.Task
import zio.ZIO
import zio.ZIOAppArgs
import zio.ZIOAppDefault
import zio.ZLayer
import zio.durationInt
import zio.logging.backend.SLF4J
import toph.module.ListenerModule

object Toph extends ZIOAppDefault:

  override val bootstrap: ZLayer[ZIOAppArgs, Any, Any] =
    Runtime.removeDefaultLoggers >>> SLF4J.slf4j

  override def run: ZIO[ZIOAppArgs & Scope, Any, Any] =
    for
      configModule <- ConfigModule()
      _            <- run(configModule)
    yield ExitCode.success

  private def run(configModule: ConfigModule): Task[Any] =
    for
      farangoModule      <- FarangoModule(configModule.toph.arango)
      repositoryModule   <- RepositoryModule(farangoModule)
      kafkaModule        <- KafkaModule(configModule.toph.kafka)
      coreModule         <- CoreModule(repositoryModule)
      listenerModule     <- ListenerModule(coreModule, kafkaModule)
      eventJournalStream <- listenerModule.eventJournalStream
      fiberEventJournal  <- eventJournalStream.runDrain.fork
      _                  <- ZIO.logInfo("🌎 Toph is ready!")
      _                  <- fiberEventJournal.join
    yield ()
