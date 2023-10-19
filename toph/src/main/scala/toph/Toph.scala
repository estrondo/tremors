package toph

import toph.config.TophConfig
import toph.module.CentreModule
import toph.module.GRPCModule
import toph.module.KafkaModule
import toph.module.SecurityModule
import tremors.zio.farango.FarangoModule
import tremors.zio.starter.ZioStarter
import zio.Scope
import zio.ZIO
import zio.ZIOAppArgs
import zio.ZIOAppDefault
import zio.ZLayer

object Toph extends ZIOAppDefault:

  override val bootstrap: ZLayer[ZIOAppArgs, Any, Scope] = ZioStarter.logging

  override def run: ZIO[ZIOAppArgs & Scope, Any, Any] =
    for
      tuple               <- ZioStarter[C]()
                               .tapErrorCause(ZIO.logErrorCause("An error happened during Toph starting up.", _))
      (C(config), profile) = tuple
      _                   <- ZIO.logInfo(s"\uD83D\uDDFA Toph is starting in [${profile.map(_.value).getOrElse("default")}].")
      farangoModule       <- FarangoModule(config.arango)
      kafkaModule         <- KafkaModule(config.kafka)
      centreModule        <- CentreModule()
      securityModule      <- SecurityModule(centreModule)
      grpcModule          <- GRPCModule(config.grpc, securityModule, centreModule)
      _                   <- grpcModule.server.launch
    yield ()

  case class C(toph: TophConfig)
