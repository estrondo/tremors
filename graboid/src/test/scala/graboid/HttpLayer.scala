package graboid

import zio.ULayer
import zio.http.Client

object HttpLayer:

  def serviceLayer: ULayer[HttpService] =
    HttpService.auto(Client.default.orDie)
