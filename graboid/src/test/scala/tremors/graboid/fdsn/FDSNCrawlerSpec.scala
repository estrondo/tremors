package tremors.graboid.fdsn

import tremors.graboid.Crawler
import tremors.graboid.CrawlerTimeline
import tremors.graboid.DockerLayer
import tremors.graboid.DockerLayer.given
import tremors.graboid.HttpService
import tremors.graboid.Spec
import tremors.graboid.WithHttpLayer
import tremors.graboid.WithHttpServiceLayer
import tremors.graboid.quakeml.QuakeMLParser
import zhttp.http.Response
import zhttp.service.ChannelFactory
import zhttp.service.EventLoopGroup
import zio.Cause
import zio.Scope
import zio.StackTrace
import zio.Task
import zio.ULayer
import zio.URLayer
import zio.ZIO
import zio.ZLayer
import zio.stream.ZSink
import zio.stream.ZStream
import zio.test.*

import java.net.URL
import java.time.ZonedDateTime
import org.testcontainers.containers.wait.strategy.Wait
import tremors.graboid.quakeml.model.Event
import tremors.graboid.quakeml.model.ResourceReference
import org.mockito.Mockito.*

object FDSNCrawlerSpec extends Spec with WithHttpServiceLayer with WithHttpLayer:

  val ExposedMockserverPort = 1090

  def spec = suite("FDSN Crawler Spec")(
    suite("Using Mockserver")(
      test("should fetch events correctly.") {
        val timeline = mock(classOf[CrawlerTimeline])
        when(timeline.lastUpdate)
          .thenReturn(ZIO.none)

        for
          port   <- DockerLayer.getPort(ExposedMockserverPort)
          config  = FDSNCrawler.Config(
                      organization = "testable",
                      queryURL = URL(s"http://localhost:$port/simple/fdsnws/event/1/query")
                    )
          crawler = FDSNCrawler(config, httpServiceLayer, timeline, QuakeMLParser())
          stream <- crawler.crawl().orDieWith(identity)
          all    <- stream.runCollect.orDieWith(identity)
        yield assertTrue(
          all.size == 3,
          all(0).asInstanceOf[Event].publicID == ResourceReference(
            "smi:org.gfz-potsdam.de/geofon/usp2022mqpz"
          ),
          all(1).asInstanceOf[Event].publicID == ResourceReference(
            "smi:org.gfz-potsdam.de/geofon/usp2022mcfz"
          ),
          all(2).asInstanceOf[Event].publicID == ResourceReference(
            "smi:org.gfz-potsdam.de/geofon/usp2022ltei"
          )
        )
      }
    ).provideLayer(dockerLayer)
  )

  def dockerLayer = DockerLayer.singleContainerLayer(
    DockerLayer.Def(
      image = "mockserver/mockserver:latest",
      env = Map(
        "SERVER_PORT" -> ExposedMockserverPort.toString()
      ),
      exposedPorts = Seq(ExposedMockserverPort),
      volumes = Seq(
        "src/test/mockserver/FDSNCrawlerSpec" -> "/config"
      ),
      waitStrategy = Wait.forLogMessage(s".*port: $ExposedMockserverPort.*", 1)
    )
  )
