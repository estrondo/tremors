package toph.module

import zio.RIO
import zio.ZIOAppArgs
import zio.ZIO
import com.softwaremill.macwire.wire
import zioapp.ZProfile
import toph.config.TophConfig
import toph.BuildInfo

trait ConfigModule

object ConfigModule:

  case class C(toph: TophConfig)

  def apply(): RIO[ZIOAppArgs, ConfigModule] =
    for
      tuple <- ZProfile.load[C](useFirstArgumentLine = false)
      _     <- ZIO.logInfo(s"🌎 Toph [${BuildInfo.version}] is going to start in [${tuple._2.getOrElse("default")}] mode.")
    yield Impl(tuple._1.toph)

  private class Impl(toph: TophConfig) extends ConfigModule
