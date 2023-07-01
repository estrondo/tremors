package graboid

import graboid.config.GraboidConfig
import graboid.module.RepositoryModule
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

      _                <- ZIO.logError(s"Starting Graboid in [${profile.map(_.value).getOrElse("default")}] mode.")
      farangoModule    <- FarangoModule(configuration.arango)
      repositoryModule <- RepositoryModule(farangoModule)
    yield ()

  final private case class C(graboid: GraboidConfig)
