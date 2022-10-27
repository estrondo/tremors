package tremors.webapi1x

import zio.ZIOAppDefault
import zio.Scope
import zio.ZIO
import zio.ZIOAppArgs
import zhttp.service.{Server, UServer}
import zio.ZIOApp.apply
import tremors.zioapp.ZProfile
import zio.{UIO, ZIO, Task}
import zhttp.service.EventLoopGroup
import zhttp.http.{HttpApp}
import zio.ExitCode

object WebApi extends ZIOAppDefault:

  case class Root(webapi: WebApiConfig)

  case class HttpConfig(
      hostname: Option[String],
      port: Option[Int],
      threads: Option[Int]
  )

  case class WebApiConfig(
      http: HttpConfig
  )

  override def run: ZIO[ZIOAppArgs & Scope, Any, ExitCode] =
    for
      root       <- ZProfile.loadOnlyConfig[Root]()
      router     <- Router(root.webapi)
      httpApp    <- router.createApp()
      httpModule <- HttpModule(root.webapi)
      _          <- httpModule.runServer(httpApp)
    yield ExitCode.success
