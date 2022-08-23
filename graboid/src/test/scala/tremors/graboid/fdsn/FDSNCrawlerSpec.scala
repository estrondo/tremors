package tremors.graboid.fdsn

import tremors.graboid.Spec
import zio.Scope
import zio.test.TestEnvironment
import zio.test
import zio.ZIO
import zio.test.*
import java.net.URL
import zio.{ULayer, URLayer, Task}
import zhttp.service.ChannelFactory
import zhttp.service.EventLoopGroup
import tremors.graboid.HttpService
import zio.mock.{Mock, Proxy}
import zhttp.http.Response
import zio.ZLayer
import tremors.graboid.MockHttpService
import zio.mock.Expectation

object FDSNCrawlerSpec extends Spec:

  def spec = suite("FDSN Crawler Spec") {

    test("should fetch some events correctly.") {
      val config = FDSNCrawler.Config(
        organization = "test",
        query = Some(URL("http://localhost"))
      )

      val layer: ULayer[HttpService] = MockHttpService
        .GetRequest(
          assertion = Assertion.equalTo(URL("http://localhost")),
          result = Expectation.value(Response.ok)
        )
        .toLayer

      val parserFactory = new QuakeMLParserFactory:
        override def apply(): QuakeMLParser = 
          throw new IllegalStateException("@@@")

      val crawler = FDSNCrawler(config, layer, parserFactory)

      for stream <- crawler.crawl()
      yield assertTrue(stream ne null)
    }
  }
