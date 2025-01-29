package tremors.zio.http

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.options
import zio.RLayer
import zio.Scope
import zio.ZIO
import zio.ZLayer

object MockServer:

  val layer: RLayer[Scope, WireMockServer] =
    def acquire =
      ZIO.attemptBlocking {
        WireMockServer(options().dynamicPort().dynamicHttpsPort())
      }

    def release(wireMockServer: WireMockServer) =
      ZIO.attemptBlocking {
        wireMockServer.shutdownServer()
      }.orDie

    ZLayer.fromZIO {
      ZIO.acquireRelease(acquire)(release)
    }
