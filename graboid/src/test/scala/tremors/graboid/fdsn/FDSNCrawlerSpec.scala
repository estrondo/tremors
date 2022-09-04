package tremors.graboid.fdsn

import tremors.graboid.{CrawlerTimeline, HttpService, Spec, WithHttpLayer, WithHttpServiceLayer}
import zhttp.http.Response
import zhttp.service.{ChannelFactory, EventLoopGroup}
import zio.{test, Scope, Task, ULayer, URLayer, ZIO, ZLayer}
import zio.stream.ZSink
import zio.test.*

import java.net.URL
import java.time.ZonedDateTime
import tremors.graboid.DockerLayer
import tremors.graboid.DockerLayer.given

object FDSNCrawlerSpec extends Spec with WithHttpServiceLayer with WithHttpLayer:

  def spec = suite("FDSN Crawler Spec")(
    test("should fetch some events correctly.") {
      val parserFactory = new QuakeMLParserFactory:
        override def apply(): QuakeMLParser =
          throw new IllegalStateException("@@@")

      val timeline = new CrawlerTimeline():
        override def lastUpdate: Task[Option[ZonedDateTime]] = ZIO.none

      for
        port   <- DockerLayer.port(8080)
        config  = FDSNCrawler.Config(
                    organization = "testable",
                    query = Some(URL(s"http://localhost:$port"))
                  )
        crawler = FDSNCrawler(config, httpServiceLayer, timeline, parserFactory)
        stream <- crawler.crawl()
        count  <- stream.runCount
      yield assertTrue(count == 0L)
    }
  ).provideLayerShared(
    DockerLayer.createLayer(
      DockerLayer.Def(
        image = "mockserver/mockserver:5.14.0",
        exposedPorts = Seq(8080),
        volumes = Seq(
          ("hello", "/hello")
        )
      )
    )
  )
