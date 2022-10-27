package tremors.webapi1x

import zio.{Task, ZIO}
import com.softwaremill.macwire.wire
import zhttp.http.*
import zhttp.http.Method
import tremors.webapi1x.handler.*

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
    Http.collectZIO { case request @ Method.GET -> !! / "about" => aboutHandler(request) }
  }
