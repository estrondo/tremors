package tremors.graboid

import java.net.URL
import zhttp.http.Response
import zio.*
import zhttp.http.Http
import zhttp.service.ChannelFactory
import zhttp.service.EventLoopGroup
import zhttp.service.Client

trait HttpService:

  def get(url: String): Task[Response]

object HttpService:

  type R = ChannelFactory & EventLoopGroup

  def auto(layer: ULayer[R]): ULayer[HttpService] =
    ZLayer.succeed(HttpServiceImpl(layer))

  def get(url: String): RIO[HttpService, Response] =
    ZIO.serviceWithZIO[HttpService](_.get(url))

private class HttpServiceImpl(layer: ULayer[HttpService.R]) extends HttpService:

  override def get(url: String): Task[Response] =
    Client
      .request(url.toString())
      .provideLayer(layer)
