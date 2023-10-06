package graboid.module

import java.time.Duration
import zio.Task
import zio.TaskLayer
import zio.ZIO
import zio.ZLayer
import zio.http.Client
import zio.http.DnsResolver
import zio.http.ZClient
import zio.http.netty.NettyConfig
import zio.http.netty.client.NettyClientDriver

trait HttpModule:

  def client: TaskLayer[Client]

object HttpModule:

  def apply(): Task[HttpModule] =
    ZIO.succeed(new Impl)

  private class Impl() extends HttpModule:

    override val client: TaskLayer[Client] =
      val configLayer = ZLayer.succeed {
        ZClient.Config.default
          .connectionTimeout(Duration.ofSeconds(10))
      }

      val nettyLayer = ZLayer.succeed(NettyConfig.default) >>> NettyClientDriver.live

      (configLayer ++ DnsResolver.default ++ nettyLayer) >>> Client.customized
