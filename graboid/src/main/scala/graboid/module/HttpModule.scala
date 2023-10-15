package graboid.module

import java.time.Duration
import zio.Task
import zio.TaskLayer
import zio.ZIO
import zio.ZLayer
import zio.http.Client
import zio.http.DnsResolver
import zio.http.ZClient
import zio.http.netty.ChannelType
import zio.http.netty.NettyConfig
import zio.http.netty.client.NettyClientDriver

trait HttpModule:

  def client: TaskLayer[Client]

object HttpModule:

  def apply(): Task[HttpModule] =
    ZIO.succeed(new Impl)

  private class Impl() extends HttpModule:

    override val client: TaskLayer[Client] =
      val config = ZLayer.succeed {
        ZClient.Config.default
          .addUserAgentHeader(false)
          .connectionTimeout(Duration.ofSeconds(10))
          .idleTimeout(Duration.ofSeconds(10))
      }

      val netty = ZLayer.succeed(
        NettyConfig.default
          .channelType(ChannelType.NIO)
      ) >>> NettyClientDriver.live

      val dns = ZLayer.succeed {
        DnsResolver.Config.default
      } >>> DnsResolver.configured()

      (config ++ dns ++ netty) >>> Client.customized
