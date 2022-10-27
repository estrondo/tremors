package tremors.webapi1x

import zio.{Task, ZIO}
import com.softwaremill.macwire.wire
import zhttp.http.HttpApp
import zhttp.service.Server
import zhttp.service.Server.Start
import zhttp.service.EventLoopGroup
import zhttp.service.server.ServerChannelFactory

trait HttpModule:

  def runServer(httpApp: HttpApp[Any, Throwable]): Task[Nothing]

object HttpModule:

  val DefaultHostname = "0.0.0.0"
  val DefaultPort     = 8080

  def apply(webapi: WebApi.WebApiConfig): Task[HttpModule] =
    ZIO.attempt(wire[HttpModuleImpl])

private[webapi1x] class HttpModuleImpl(config: WebApi.WebApiConfig) extends HttpModule:

  import HttpModule.*

  private def eventLoopGroup = EventLoopGroup.nio(
    nThreads =
      math.max(config.http.threads.getOrElse(-1), Runtime.getRuntime().availableProcessors())
  )

  private def serverChannelFactory = ServerChannelFactory.nio

  override def runServer(httpApp: HttpApp[Any, Throwable]): Task[Nothing] =
    Server(httpApp)
      .withBinding(
        config.http.hostname.getOrElse(DefaultHostname),
        config.http.port.getOrElse(DefaultPort)
      )
      .start
      .provideLayer(eventLoopGroup ++ serverChannelFactory)
