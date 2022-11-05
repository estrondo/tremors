package graboid

import zio.ULayer
import zhttp.service.ChannelFactory
import zhttp.service.EventLoopGroup

object HttpLayer:

  def httpLayer: ULayer[ChannelFactory & EventLoopGroup] =
    ChannelFactory.nio ++ EventLoopGroup.nio(nThreads = 2)

  def serviceLayer: ULayer[HttpService] =
    HttpService.auto(httpLayer)
