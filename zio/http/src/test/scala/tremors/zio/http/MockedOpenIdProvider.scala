package tremors.zio.http

import com.github.tomakehurst.wiremock.WireMockServer
import zio.RLayer
import zio.ZIO
import zio.ZLayer

class MockedOpenIdProvider(wireMockServer: WireMockServer):

  val id = "mocked-openid-provider"

  val discoveryEndpoint: String = wireMockServer.url(s"/$id/.well-known/openid-configuration")

object MockedOpenIdProvider:

  val layer: RLayer[WireMockServer, MockedOpenIdProvider] =
    ZLayer.fromZIO {
      ZIO.serviceWith[WireMockServer](MockedOpenIdProvider(_))
    }
