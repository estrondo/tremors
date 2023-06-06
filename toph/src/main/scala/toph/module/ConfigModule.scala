package toph.module

import toph.BuildInfo
import toph.config.TophConfig
import zio.RIO
import zio.ZIO
import zio.ZIOAppArgs
import zio.config.magnolia.Descriptor
import zioapp.ZProfile
import zioapp.ZProfile.given

trait ConfigModule:
  def toph: TophConfig

object ConfigModule:

  case class C(toph: TophConfig)

  def apply(): RIO[ZIOAppArgs, ConfigModule] =
    for
      tuple <- ZProfile.load[C](useFirstArgumentLine = true)
      _     <- ZIO.logInfo(s"🌎 Toph [${BuildInfo.version}] is going to start in [${tuple._2.getOrElse("default")}] mode.")
    yield Impl(tuple._1.toph)

  private class Impl(override val toph: TophConfig) extends ConfigModule
