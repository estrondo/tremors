package tremors.graboid

import zio.ULayer
import zhttp.service.ChannelFactory

trait WithHttpServiceLayer:

  self: WithHttpLayer =>

  def httpServiceLayer: ULayer[HttpService] =
    HttpService.auto(httpLayer)
