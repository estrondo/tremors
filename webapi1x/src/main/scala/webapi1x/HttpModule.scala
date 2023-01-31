package webapi1x

import com.softwaremill.macwire.wire
import zhttp.http.HttpApp
import zhttp.service.EventLoopGroup
import zhttp.service.Server
import zhttp.service.Server.Start
import zhttp.service.server.ServerChannelFactory
import zio.Task
import zio.ZIO

trait HttpModule:

  def runServer(httpApp: HttpApp[Any, Throwable]): Task[Nothing]

object HttpModule:

  val DefaultHostname = "0.0.0.0"
  val DefaultPort     = 8080

  def apply(webapi: WebApi.WebApiConfig): Task[HttpModule] =
    ZIO.attempt(wire[Impl])

  private[webapi1x] class Impl(config: WebApi.WebApiConfig) extends HttpModule:

    import HttpModule.*

    private def eventLoopGroup = EventLoopGroup.nio(
      nThreads = math.max(config.http.threads.getOrElse(-1), Runtime.getRuntime().availableProcessors())
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
