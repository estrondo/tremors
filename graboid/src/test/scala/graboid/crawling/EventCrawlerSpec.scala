package graboid.crawling

import graboid.DataCentre
import graboid.GraboidSpec
import graboid.http.UpdateQueryParam
import graboid.manager.DataCentreFixture
import izumi.reflect.Tag
import one.estrondo.sweetmockito.zio.SweetMockitoLayer
import org.mockito.Mockito
import tremors.generator.KeyGenerator
import zio.ZIO
import zio.ZLayer
import zio.http.Body
import zio.http.Client
import zio.http.Request
import zio.http.Response
import zio.http.URL
import zio.kafka.consumer.Consumer
import zio.kafka.consumer.Subscription
import zio.kafka.producer.Producer
import zio.kafka.serde.Serde
import zio.kafka.testkit.Kafka
import zio.kafka.testkit.KafkaTestUtils
import zio.test.assertTrue

object EventCrawlerSpec extends GraboidSpec:

  def spec = suite("EventCrawlerSpec")(
    test("It should find one event.") {

      val query = EventCrawlingQueryFixture.createRandom()

      for
        requests <- makeRequests(query)
        _        <- updateHttpClientMock(requests.toSeq)
        result   <- ZIO.serviceWithZIO[EventCrawler](_.apply(query).runCollect)
      yield assertTrue(result.size == 1)
    },
    test("It should produce one record on Kafka.") {

      val query = EventCrawlingQueryFixture.createRandom()

      for
        requests <- makeRequests(query)
        _        <- updateHttpClientMock(requests.toSeq)
        _        <- ZIO.serviceWithZIO[EventCrawler](_.apply(query).runCollect)
        records  <- Consumer
                      .plainStream(Subscription.topics(EventCrawler.GraboidEventTopic), Serde.string, Serde.byteArray)
                      .take(1)
                      .runCollect
                      .provideSome[Kafka](
                        KafkaTestUtils.consumer(clientId = "test", groupId = Some("test-group"))
                      )
      yield assertTrue(records.size == 1)
    }
  ).provideSome(
    KafkaTestUtils.producer,
    Kafka.embedded,
    SweetMockitoLayer.newMockLayer[Client],
    ZLayer.succeed(DataCentreFixture.createRandom()),
    SweetMockitoLayer.newMockLayer[KeyGenerator],
    ZLayer {
      for
        dataCentre   <- ZIO.service[DataCentre]
        keyGenerator <- ZIO.service[KeyGenerator]
        eventCrawler <- EventCrawler(dataCentre, keyGenerator)
      yield eventCrawler
    }
  )

  private def makeRequests(query: EventCrawlingQuery) =
    for
      dataCentre <- ZIO.service[DataCentre]
      url        <- ZIO.fromEither(URL.decode(dataCentre.event.get))
      params     <- UpdateQueryParam(query, url.queryParams)
    yield for param <- params yield Request.get(url.addQueryParams(param))

  private def updateHttpClientMock(requests: Seq[Request]) =
    val stream   = readFile("test-data/quakeml-01.xml")
    val response = Response.ok.copy(body = Body.fromStream(stream))

    ZIO.serviceWith[Client] { client =>
      Mockito
        .when(client.request(any())(any(), any()))
        .thenReturn(ZIO.succeed(response))
    } as response
