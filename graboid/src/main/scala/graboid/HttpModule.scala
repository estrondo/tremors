package graboid

import graboid.config.*
import zhttp.service.ChannelFactory
import zhttp.service.EventLoopGroup
import zio.Task
import zio.TaskLayer
import zio.ULayer
import zio.ZIO
import zio.ZLayer
import zio.ZLayer.apply

trait HttpModule:

  val channelFactory: ULayer[ChannelFactory] = ChannelFactory.nio

  def eventLoopGroup: ULayer[EventLoopGroup]

  def serviceLayer: ULayer[HttpService]

object HttpModule:

  def apply(clientConfig: HttpClientConfig): Task[HttpModule] = ZIO.attempt {
    Impl(clientConfig.parallelism)
  }

  private[graboid] class Impl(parallelism: Int) extends HttpModule:

    val eventLoopGroup: ULayer[EventLoopGroup] = EventLoopGroup.nio(parallelism)

    override def serviceLayer: ULayer[HttpService] =
      HttpService.auto(channelFactory ++ eventLoopGroup)
