package tremors.graboid

import java.net.URL
import zhttp.http.Response
import zio.*
import zhttp.http.Http

trait HttpService:

  def get(url: URL): Task[Response]

object HttpService:

  def get(url: URL): ZIO[HttpService, Throwable, Response] =
    ZIO.serviceWithZIO[HttpService](_.get(url))

private class HttpServiceImpl extends HttpService:

  override def get(url: URL): Task[Response] = ???
