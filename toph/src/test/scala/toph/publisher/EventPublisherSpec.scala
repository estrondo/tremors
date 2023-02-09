package toph.publisher

import toph.Spec
import toph.fixture.EpicentreFixture
import toph.fixture.EventFixture
import toph.fixture.HypocentreFixture
import toph.kafka.TophEventJournalTopic
import toph.message.protocol.EventJournalMessage
import toph.message.protocol.NewEvent
import zio.Scope
import zio.ZIO
import zio.ZLayer
import zio.stream.ZStream
import zio.test.TestEnvironment
import zio.test.assertTrue
import zkafka.KafkaManager
import zkafka.KafkaMessage

object EventPublisherSpec extends Spec:

  override def spec: zio.test.Spec[TestEnvironment & Scope, Any] =
    suite("An EventPublisher")(
      test("It should offer a event with hypocentre and epicentre in the stream.") {
        val event      = EventFixture.createRandom()
        val epicentre  = EpicentreFixture.createRandom()
        val hypocentre = HypocentreFixture.createRandom()
        val newEvent   = NewEvent(
          key = event.key
        )

        for
          publisher <- ZIO.service[EventPublisher]
          result    <- publisher.accept(event.key, (event, Seq(epicentre -> Some(hypocentre))))
        yield assertTrue(
          result == Seq(KafkaMessage(newEvent, Some(event.key), TophEventJournalTopic))
        )
      }
    ).provideSome(
      ZLayer.succeed(EventPublisher())
    )
