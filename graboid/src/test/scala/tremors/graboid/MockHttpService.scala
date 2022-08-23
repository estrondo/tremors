package tremors.graboid

import zio.mock._
import zio._
import zio.test._
import java.net.URL
import zhttp.http.Response
import zio.URLayer
import zio.Task

object MockHttpService extends Mock[HttpService]:

  object GetRequest extends Effect[URL, Throwable, Response]

  val compose: URLayer[Proxy, HttpService] =
    ZLayer.fromFunction { (proxy: Proxy) =>
      new HttpService:
        override def get(url: URL): Task[Response] =
          proxy(GetRequest, url)
    }
