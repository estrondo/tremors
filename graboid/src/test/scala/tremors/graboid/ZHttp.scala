package tremors.graboid

import zio.ULayer
import zhttp.service.ChannelFactory
import zhttp.service.EventLoopGroup

def httpLayer: ULayer[ChannelFactory & EventLoopGroup] =
  ChannelFactory.nio ++ EventLoopGroup.nio(nThreads = 2)

def httpServiceLayer: ULayer[HttpService] =
  HttpService.auto(httpLayer)
