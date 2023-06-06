package toph.publisher

import toph.Spec
import toph.fixture.EventDataFixture
import toph.fixture.HypocentreDataFixture
import toph.fixture.MagnitudeDataFixture
import toph.kafka.TophEventJournalTopic
import toph.message.protocol.NewEvent
import zio.Scope
import zio.ZIO
import zio.ZLayer
import zio.test.TestEnvironment
import zio.test.assertTrue
import zkafka.KafkaMessage

object EventPublisherSpec extends Spec:

  override def spec: zio.test.Spec[TestEnvironment & Scope, Any] =
    suite("An EventPublisher")(
      test("It should offer a event with hypocentre in the stream.") {
        val event      = EventDataFixture.createRandom()
        val hypocentre = HypocentreDataFixture.createRandom()
        val magnitude  = MagnitudeDataFixture.createRandom()
        val newEvent   = NewEvent(
          key = event.key
        )

        for
          publisher <- ZIO.service[EventPublisher]
          result    <- publisher.accept(event.key, (event, Seq(hypocentre), Seq(magnitude)))
        yield assertTrue(
          result == Seq(KafkaMessage(newEvent, Some(event.key), TophEventJournalTopic))
        )
      }
    ).provideSome(
      ZLayer.succeed(EventPublisher())
    )
