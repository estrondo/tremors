package webapi.module

import com.softwaremill.macwire.wire
import webapi.config.WebAPIConfig
import zio.RIO
import zio.ZIO
import zio.ZIOAppArgs
import zioapp.ZProfile
import zioapp.ZProfile.given

object ConfigModule:

  private case class C(webapi: WebAPIConfig)

  def apply(): RIO[ZIOAppArgs, WebAPIConfig] =
    for
      tuple    <- ZProfile.load[C](useFirstArgumentLine = true)
      (c, mode) = tuple
      _        <- ZIO.logInfo(s"🐧 WebAPI is starting in [${mode.getOrElse("default")}] mode.")
    yield c.webapi
