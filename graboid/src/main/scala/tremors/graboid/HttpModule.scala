package tremors.graboid

import zio.{ULayer, Task, ZIO}

trait HttpModule:

  def serviceLayer: ULayer[HttpService]

object HttpModule:

  def apply(): Task[HttpModule] = ZIO.attempt {
    HttpModuleImpl()
  }

private[graboid] class HttpModuleImpl extends HttpModule:

  override def serviceLayer: ULayer[HttpService] = throw IllegalStateException("serviceLayer")
