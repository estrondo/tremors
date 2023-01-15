package webapi1x

import com.softwaremill.macwire.wire
import webapi1x.handler.*
import zhttp.http.*
import zio.Task
import zio.UIO
import zio.ZIO

trait RouterModule:

  def createApp(): Task[HttpApp[Any, Throwable]]

object RouterModule:

  def apply(
      crawlerModule: GraboidModule
  ): Task[RouterModule] = ZIO.attempt(wire[RouterModuleImpl])

private[webapi1x] class RouterModuleImpl(
    crawlerModule: GraboidModule
) extends RouterModule:

  private val aboutHandler = AboutHandler()

  override def createApp(): Task[HttpApp[Any, Throwable]] = ZIO.attempt {
    Http.collectZIO {
      case req @ Method.GET -> !! / "about" => aboutHandler(req)
      case req                              => notFound(req)
    }
  }

  private def notFound(request: Request): UIO[Response] =
    ZIO.succeed(Response(status = Status.NotFound, body = Body.fromCharSequence("Not Found!")))
