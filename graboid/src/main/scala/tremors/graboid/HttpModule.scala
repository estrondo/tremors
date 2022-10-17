package tremors.graboid

import zio.ULayer

trait HttpModule:

  def serviceLayer: ULayer[HttpService]
