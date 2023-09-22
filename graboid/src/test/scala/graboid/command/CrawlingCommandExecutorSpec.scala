package graboid.command

import graboid.GraboidSpec
import graboid.crawling.CrawlingExecutor
import graboid.crawling.EventCrawlingQuery
import graboid.manager.DataCentreFixture
import graboid.manager.DataCentreManager
import graboid.protocol.RunDataCentreEventCrawling
import graboid.protocol.RunEventCrawling
import java.time.Duration
import one.estrondo.sweetmockito.zio.SweetMockitoLayer
import one.estrondo.sweetmockito.zio.given
import org.mockito.Mockito
import org.mockito.Mockito.verify
import scala.util.Random
import tremors.ZonedDateTimeFixture
import tremors.generator.KeyGenerator
import tremors.generator.KeyLength
import zio.ZIO
import zio.ZLayer
import zio.http.Client
import zio.kafka.producer.Producer
import zio.stream.ZStream
import zio.test.assertTrue

object CrawlingCommandExecutorSpec extends GraboidSpec:

  def spec = suite("CrawlingCommandExecutorSpec")(
    test(s"It should execute a ${classOf[RunDataCentreEventCrawling].getSimpleName}.") {

      val (command, dataCentre) = createRandomRunDataCentreEventCrawling()
      val expectedQuery         = EventCrawlingQuery(
        starting = command.starting,
        ending = command.ending,
        timeWindow = command.timeWindow,
        queries = Seq(
          EventCrawlingQuery.Query(
            magnitudeType = command.magnitudeType,
            eventType = command.eventType,
            min = command.minMagnitude,
            max = command.maxMagnitude
          )
        )
      )

      for
        executor <- ZIO.service[CrawlingExecutor]
        _         = Mockito.when(executor.execute(dataCentre, expectedQuery)).thenReturn(ZStream.empty)
        _        <- SweetMockitoLayer[DataCentreManager].whenF2(_.get(dataCentre.id)).thenReturn(Some(dataCentre))
        _        <- ZIO.serviceWithZIO[CrawlingCommandExecutor](_.apply(command))
      yield assertTrue(
        verify(executor).execute(dataCentre, expectedQuery) == null
      )
    },
    test(s"It should execute a ${classOf[RunEventCrawling].getSimpleName}.") {

      val command       = createRandomRunEventCrawling()
      val expectedQuery = EventCrawlingQuery(
        starting = command.starting,
        ending = command.ending,
        timeWindow = command.timeWindow,
        queries = Seq(
          EventCrawlingQuery.Query(
            magnitudeType = command.magnitudeType,
            eventType = command.eventType,
            min = command.minMagnitude,
            max = command.maxMagnitude
          )
        )
      )

      for
        executor <- ZIO.service[CrawlingExecutor]
        _         = Mockito.when(executor.execute(expectedQuery)).thenReturn(ZStream.empty)
        _        <- ZIO.serviceWithZIO[CrawlingCommandExecutor](_.apply(command))
      yield assertTrue(
        verify(executor).execute(expectedQuery) == null
      )
    }
  ).provideSome(
    SweetMockitoLayer.newMockLayer[CrawlingExecutor],
    SweetMockitoLayer.newMockLayer[DataCentreManager],
    SweetMockitoLayer.newMockLayer[Client],
    SweetMockitoLayer.newMockLayer[Producer],
    ZLayer {
      for
        executor          <- ZIO.service[CrawlingExecutor]
        dataCentreManager <- ZIO.service[DataCentreManager]
        client            <- ZIO.service[Client]
        producer          <- ZIO.service[Producer]
        layer              = ZLayer.succeed(client) ++ ZLayer.succeed(producer)
        crawlingExecutor  <-
          CrawlingCommandExecutor(executor, dataCentreManager, layer)
      yield crawlingExecutor
    }
  )

  def createRandomRunDataCentreEventCrawling() =
    val starting   = ZonedDateTimeFixture.createRandom()
    val dataCentre = DataCentreFixture.createRandom()

    RunDataCentreEventCrawling(
      commandId = KeyGenerator.generate(KeyLength.Medium),
      dataCentre = dataCentre.id,
      starting = starting,
      ending = starting.plusDays(2),
      timeWindow = Duration.ofMinutes(Random.nextInt(10)),
      minMagnitude = Some(Random.nextDouble() * 7),
      maxMagnitude = Some(Random.nextDouble() * 3),
      magnitudeType = Some("high"),
      eventType = Some("earthquake")
    ) -> dataCentre

  def createRandomRunEventCrawling() =
    val starting = ZonedDateTimeFixture.createRandom()

    RunEventCrawling(
      commandId = KeyGenerator.generate(KeyLength.Medium),
      starting = starting,
      ending = starting.plusDays(2),
      timeWindow = Duration.ofMinutes(Random.nextInt(10)),
      minMagnitude = Some(Random.nextDouble() * 7),
      maxMagnitude = Some(Random.nextDouble() * 3),
      magnitudeType = Some("high"),
      eventType = Some("earthquake")
    )
