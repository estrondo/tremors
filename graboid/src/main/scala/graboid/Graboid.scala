package graboid

import graboid.config.GraboidConfig
import graboid.module.CommandModule
import graboid.module.CrawlingModule
import graboid.module.HttpModule
import graboid.module.KafkaModule
import graboid.module.ListenerModule
import graboid.module.ManagerModule
import graboid.module.RepositoryModule
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.geom.PrecisionModel
import tremors.zio.farango.FarangoModule
import tremors.zio.starter.ZioStarter
import zio.Scope
import zio.ZIO
import zio.ZIOAppArgs
import zio.ZIOAppDefault
import zio.ZLayer

object Graboid extends ZIOAppDefault:

  override val bootstrap: ZLayer[ZIOAppArgs, Any, Scope] =
    ZioStarter.logging

  override def run: ZIO[ZIOAppArgs with Scope, Any, Any] =
    for
      tuple                      <- ZioStarter[C]()
                                      .tapErrorCause(ZIO.logErrorCause("It was impossible to configure Graboid!", _))
      (C(configuration), profile) = tuple

      geometryFactory <- ZIO.attempt(GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), 4326))

      _                <- ZIO.logInfo(s"Starting Graboid in [${profile.map(_.value).getOrElse("default")}] mode.")
      httpModule       <- HttpModule()
      farangoModule    <- FarangoModule(configuration.arango, geometryFactory)
      repositoryModule <- RepositoryModule(farangoModule)
      managerModule    <- graboid.module.ManagerModule(repositoryModule)
      kafkaModule      <- KafkaModule("graboid", configuration.kafka)
      crawlingModule   <- CrawlingModule(configuration.crawling, httpModule, kafkaModule, managerModule, repositoryModule)
      commandModule    <- CommandModule(managerModule, crawlingModule, kafkaModule.producerLayer ++ httpModule.client)
      listenerModule   <- ListenerModule(commandModule, kafkaModule)
      crawlingFiber    <- crawlingModule.start().fork
      _                <- ZLayer.fromZIO(listenerModule.commandResultStream.runDrain).launch
    yield ()

  final private case class C(graboid: GraboidConfig)
