package webapi

import webapi.module.ConfigModule
import webapi.module.FarangoModule
import webapi.module.KafkaModule
import zio.Scope
import zio.ZIO
import zio.ZIOAppArgs
import zio.ZIOAppDefault
import webapi.module.ServiceModule
import webapi.module.CoreModule
import webapi.module.RepositoryModule

object WebAPI extends ZIOAppDefault:

  override def run: ZIO[ZIOAppArgs & Scope, Any, Any] =
    for
      config           <- ConfigModule()
      farangoModule    <- FarangoModule(config.arango)
      kafkaManager     <- KafkaModule(config.kafka)
      repositoryModule <- RepositoryModule(farangoModule)
      coreModule       <- CoreModule(repositoryModule)
      serviceModule    <- ServiceModule(config.service, coreModule)
      _                <- ZIO.logInfo("🐧 Tremors WebAPI is ready. Grab your popcorn, soda and enjoy it!")
      _                <- serviceModule.server.launch
    yield exit
