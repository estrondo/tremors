package graboid
import graboid.crawling.CrawlingQueryFixture
import graboid.crawling.EventCrawler
import graboid.manager.DataCentreFixture

import java.io.File
import one.estrondo.sweetmockito.zio.SweetMockitoLayer
import one.estrondo.sweetmockito.zio.given
import org.apache.kafka.clients.producer.ProducerRecord
import org.mockito.Mockito
import org.mockito.Mockito.verify
import zio.Scope
import zio.ZIO
import zio.ZLayer
import zio.http.*
import zio.kafka.producer.Producer
import zio.test.Spec
import zio.test.assertTrue

object CrawlerSpec extends GraboidSpec:

  override def spec =
    suite("CrawlerSpec")(
      test("It should read a event.") {
        val query = CrawlingQueryFixture.createRandom()

        for
          crawler    <- ZIO.service[EventCrawler]
          dataCentre <- ZIO.service[DataCentre]

          url           <- ZIO.fromEither(URL.decode(dataCentre.event.get))
          newQueryParams = url.queryParams
                             .add("starttime", query.starting.toString)
                             .add("endtime", query.ending.toString)
                             .add("maxmagnitude", query.maxMagnitude.get.toString)
                             .add("minmagnitude", query.minMagnitude.get.toString)
          newUrl         = url.withQueryParams(newQueryParams)

          expectedRequest  = Request.get(newUrl)
          expectedResponse = Response(Status.Ok, body = Body.fromFile(File("test-data/quakeml-01.xml")))
          _               <- SweetMockitoLayer[Client]
                               .whenF2(_.request(eqTo(expectedRequest))(any(), any()))
                               .thenReturn(expectedResponse)

          events <- crawler(query).runCollect
        yield assertTrue(
          events.size == 1
        )
      },
      test("It should produce a record.") {
        val query = CrawlingQueryFixture.createRandom()

        for
          crawler    <- ZIO.service[EventCrawler]
          dataCentre <- ZIO.service[DataCentre]

          url           <- ZIO.fromEither(URL.decode(dataCentre.event.get))
          newQueryParams = url.queryParams
                             .add("starttime", query.starting.toString)
                             .add("endtime", query.ending.toString)
                             .add("maxmagnitude", query.maxMagnitude.get.toString)
                             .add("minmagnitude", query.minMagnitude.get.toString)
          newUrl         = url.withQueryParams(newQueryParams)

          expectedRequest  = Request.get(newUrl)
          expectedResponse = Response(Status.Ok, body = Body.fromFile(File("test-data/quakeml-01.xml")))
          _               <- SweetMockitoLayer[Client]
                               .whenF2(_.request(eqTo(expectedRequest))(any(), any()))
                               .thenReturn(expectedResponse)

          _        <- crawler(query).runDrain
          producer <- ZIO.service[Producer]
        yield assertTrue(
          verify(producer).produce(any[ProducerRecord[String, Array[Byte]]](), any(), any()) == null
        )
      }
    ).provideSome[Scope](
      ZLayer.succeed(DataCentreFixture.createRandom()),
      SweetMockitoLayer.newMockLayer[Client],
      SweetMockitoLayer.newMockLayer[Producer],
      ZLayer {
        ZIO.serviceWithZIO[DataCentre](EventCrawler.apply)
      }
    )
