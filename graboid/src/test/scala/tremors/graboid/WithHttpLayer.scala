package tremors.graboid

import zio.ULayer
import zhttp.service.EventLoopGroup
import zhttp.service.ChannelFactory

trait WithHttpLayer:

  def httpLayer: ULayer[ChannelFactory & EventLoopGroup] =
    ChannelFactory.nio ++ EventLoopGroup.auto(nThreads = 2)
