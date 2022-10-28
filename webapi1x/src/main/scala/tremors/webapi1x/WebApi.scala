package tremors.webapi1x

import tremors.zioapp.ZProfile
import zio.ExitCode
import zio.Scope
import zio.Task
import zio.ZIO
import zio.ZIOAppArgs
import zio.ZIOAppDefault

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
