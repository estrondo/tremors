package graboid

import zio.*
import zio.http.Client
import zio.http.Response

trait HttpService:

  def get(url: String): Task[Response]

object HttpService:

  def auto(layer: ULayer[Client]): ULayer[HttpService] =
    ZLayer.succeed(new Impl(layer))

  def get(url: String): RIO[HttpService, Response] =
    ZIO.serviceWithZIO[HttpService](_.get(url))

  private class Impl(layer: ULayer[Client]) extends HttpService:

    override def get(url: String): Task[Response] =
      Client
        .request(url.toString())
        .provideLayer(layer)
