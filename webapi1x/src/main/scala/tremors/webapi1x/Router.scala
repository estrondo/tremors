package tremors.webapi1x

import com.softwaremill.macwire.wire
import tremors.webapi1x.handler.*
import zhttp.http.Method
import zhttp.http.*
import zio.Task
import zio.UIO
import zio.ZIO

trait Router:

  def createApp(): Task[HttpApp[Any, Throwable]]

object Router:

  def apply(config: WebApi.WebApiConfig): Task[Router] =
    ZIO.attempt(wire[RouterImpl])

private[webapi1x] class RouterImpl(
    config: WebApi.WebApiConfig
) extends Router:

  private val aboutHandler = AboutHandler()

  override def createApp(): Task[HttpApp[Any, Throwable]] = ZIO.attempt {
    Http.collectZIO {
      case request @ Method.GET -> !! / "about" => aboutHandler(request)
      case request                              => notFound(request)
    }
  }

  private def notFound(request: Request): UIO[Response] =
    ZIO.succeed(Response(status = Status.NotFound, body = Body.fromCharSequence("Not Found!")))
