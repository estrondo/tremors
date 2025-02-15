package toph.module

import zio.Scope
import zio.Task
import zio.TaskLayer
import zio.ZIO
import zio.ZLayer
import zio.http.Client
import zio.http.DnsResolver
import zio.http.ZClient
import zio.http.netty.NettyConfig

class HttpModule(
    val clientLayer: TaskLayer[Client & Scope],
)

object HttpModule:

  def apply(): Task[HttpModule] = ZIO.attempt {
    val dnsResolver = DnsResolver.default
    val nettyConfig = ZLayer.succeed(NettyConfig.default)
    val myConfig    = ZLayer.succeed(ZClient.Config.default)
    val clientLayer = ((dnsResolver ++ nettyConfig ++ myConfig) >>> Client.live) ++ Scope.default
    new HttpModule(
      clientLayer = clientLayer,
    )
  }
