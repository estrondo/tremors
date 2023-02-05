package toph

import toph.module.ConfigModule
import zio.Scope
import zio.ZIO
import zio.ZIOAppArgs
import zio.ZIOAppDefault
import zio.ExitCode
import zio.Task

object Toph extends ZIOAppDefault:

  override def run: ZIO[ZIOAppArgs & Scope, Any, Any] =
    for
      configModule <- ConfigModule()
      _            <- run(configModule)
    yield ExitCode.success

  private def run(configModule: ConfigModule): Task[Unit] =
    ZIO.succeed(())
