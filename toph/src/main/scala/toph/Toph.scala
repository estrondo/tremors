package toph

import toph.module.ConfigModule
import toph.module.CoreModule
import toph.module.FarangoModule
import toph.module.GRPCModule
import toph.module.KafkaModule
import toph.module.ListenerModule
import toph.module.RepositoryModule
import toph.module.ServiceModule
import zio.ExitCode
import zio.Runtime
import zio.Scope
import zio.Task
import zio.ZIO
import zio.ZIOAppArgs
import zio.ZIOAppDefault
import zio.ZLayer
import zio.logging.backend.SLF4J

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
      serviceModule      <- ServiceModule(coreModule)
      gRPCModule         <- GRPCModule(configModule.toph.grpc, serviceModule)
      eventJournalStream <- listenerModule.eventJournalStream
      fiberEventJournal  <- eventJournalStream.runDrain.fork
      _                  <- ZIO.logInfo("🌎 Toph is ready!")
      _                  <- gRPCModule.run()
      _                  <- fiberEventJournal.join
    yield ()
