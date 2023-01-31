package graboid

import zhttp.http.Http
import zhttp.http.Response
import zhttp.service.ChannelFactory
import zhttp.service.Client
import zhttp.service.EventLoopGroup
import zio.*

import java.net.URL

trait HttpService:

  def get(url: String): Task[Response]

object HttpService:

  def auto(layer: ULayer[ChannelFactory & EventLoopGroup]): ULayer[HttpService] =
    ZLayer.succeed(Impl(layer))

  def get(url: String): RIO[HttpService, Response] =
    ZIO.serviceWithZIO[HttpService](_.get(url))

  private class Impl(layer: ULayer[ChannelFactory & EventLoopGroup]) extends HttpService:

    override def get(url: String): Task[Response] =
      Client
        .request(url.toString())
        .provideLayer(layer)
