package tremors.graboid.fdsn

import tremors.graboid.{CrawlerTimeline, HttpService, Spec, WithHttpLayer, WithHttpServiceLayer}
import zhttp.http.Response
import zhttp.service.{ChannelFactory, EventLoopGroup}
import zio.{test, Scope, Task, ULayer, URLayer, ZIO, ZLayer}
import zio.stream.ZSink
import zio.test.*

import java.net.URL
import java.time.ZonedDateTime

object FDSNCrawlerSpec extends Spec with WithHttpServiceLayer with WithHttpLayer:

  def spec = suite("FDSN Crawler Spec") {

    test("should fetch some events correctly.") {
      val config = FDSNCrawler.Config(
        organization = "test",
        query = Some(URL("http://localhost"))
      )

      val parserFactory = new QuakeMLParserFactory:
        override def apply(): QuakeMLParser =
          throw new IllegalStateException("@@@")

      val timeline = new CrawlerTimeline():
        override def lastUpdate: Task[Option[ZonedDateTime]] = ZIO.none

      val crawler = FDSNCrawler(config, httpServiceLayer, timeline, parserFactory)

      for
        stream <- crawler.crawl()
        x      <- stream.run(ZSink.count)
      yield assertTrue(x == 0L)
    }
  }
