package graboid

import graboid.config.GraboidConfig
import tremors.zio.starter.ZioStarter
import zio.Scope
import zio.ZIO
import zio.ZIOAppArgs
import zio.ZIOAppDefault
import zio.ZLayer

object Graboid extends ZIOAppDefault:

  final private case class C(graboid: GraboidConfig)

  override val bootstrap: ZLayer[ZIOAppArgs, Any, Scope] =
    ZioStarter.logging
  override def run: ZIO[ZIOAppArgs with Scope, Any, Any] =
    for
      tuple                      <- ZioStarter[C]()
      (C(configuration), profile) = tuple
      _                          <- ZIO.logDebug(s"Starting graboid in [${profile.map(_.value).getOrElse("default")}] mode.")
    yield ()
