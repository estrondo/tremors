package toph

import toph.module.ConfigModule
import zio.Scope
import zio.ZIO
import zio.ZIOAppArgs
import zio.ZIOAppDefault
import zio.ExitCode
import zio.Task
import toph.module.FarangoModule
import zio.ZLayer
import zio.Runtime
import zio.logging.backend.SLF4J
import toph.module.RepositoryModule
import zio.Schedule
import zio.durationInt
import toph.module.KafkaModule

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
      farangoModule    <- FarangoModule(configModule.toph.arango)
      repositoryModule <- RepositoryModule(farangoModule)
      kafkaModule      <- KafkaModule(configModule.toph.kafka)
    yield ()
