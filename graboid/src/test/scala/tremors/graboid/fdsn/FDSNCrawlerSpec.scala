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

object FDSNCrawlerSpec extends Spec with WithHttpServiceLayer with WithHttpLayer:

  val ExposedMockserverPort = 1090

  def spec = suite("FDSN Crawler Spec")(
    test("should fetch some events correctly.") {
      val timeline = new CrawlerTimeline():
        override def lastUpdate: Task[Option[ZonedDateTime]] = ZIO.none

      for
        port   <- DockerLayer.singleContainerPort(ExposedMockserverPort)
        config  = FDSNCrawler.Config(
                    organization = "testable",
                    queryURL = URL(s"http://localhost:$port/fdsnws/event/1/query")
                  )
        crawler = FDSNCrawler(config, httpServiceLayer, timeline, QuakeMLParser())
        stream <- crawler.crawl().orDieWith(identity)
        count  <- stream.runCount.orDieWith(identity)
      yield assertTrue(count == 1L)
    }
  ).provideLayerShared(dockerLayer)

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
