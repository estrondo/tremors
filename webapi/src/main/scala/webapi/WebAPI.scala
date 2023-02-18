package webapi

import webapi.module.ConfigModule
import webapi.module.FarangoModule
import webapi.module.HttpModule
import webapi.module.KafkaModule
import zio.Scope
import zio.ZIO
import zio.ZIOAppArgs
import zio.ZIOAppDefault

object WebAPI extends ZIOAppDefault:

  override def run: ZIO[ZIOAppArgs & Scope, Any, Any] =
    for
      config          <- ConfigModule()
      farangoModule   <- FarangoModule(config.arango)
      kafkaManager    <- KafkaModule(config.kafka)
      httpModuleLayer <- HttpModule(config.http)
      _               <- ZIO.logInfo("🐧 Tremors WebAPI is ready. Grab your popcorn, soda and enjoy it!")
      exit            <- httpModuleLayer.launch
    yield exit
