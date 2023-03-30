package graboid

import graboid.config.*
import zio.Task
import zio.TaskLayer
import zio.ULayer
import zio.ZIO
import zio.ZLayer
import zio.ZLayer.apply
import zio.http.Client

trait HttpModule:

  def serviceLayer: ULayer[HttpService]

object HttpModule:

  def apply(clientConfig: HttpClientConfig): Task[HttpModule] = ZIO.attempt {
    Impl(clientConfig.parallelism)
  }

  private[graboid] class Impl(parallelism: Int) extends HttpModule:

    override def serviceLayer: ULayer[HttpService] =
      HttpService.auto(Client.default.orDie)
