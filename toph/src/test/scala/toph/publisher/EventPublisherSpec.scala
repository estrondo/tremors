package toph.publisher

import toph.Spec
import zio.Scope
import zio.test.TestEnvironment
import zio.test.assertTrue
import zio.ZIO
import toph.fixture.EventFixture
import toph.fixture.EpicentreFixture
import toph.fixture.HypocentreFixture
import zio.ZLayer
import zio.stream.ZStream
import toph.message.protocol.EventJournalMessage
import toph.message.protocol.NewEvent

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
          result    <- publisher.publish(event, Seq(epicentre -> Some(hypocentre)))
          stream    <- ZIO.service[ZStream[Any, Throwable, EventJournalMessage]]
          published <- stream.runHead
        yield assertTrue(
          result == (event, Seq(epicentre -> Some(hypocentre))),
          published == Some(newEvent)
        )
      }
    ).provideSome(
      testLayer
    )

  private val testLayer =
    val source    = ZLayer(EventPublisher())
    val publisher = ZLayer(ZIO.serviceWith[(EventPublisher, ZStream[Any, Throwable, EventJournalMessage])](_._1))
    val stream    = ZLayer(ZIO.serviceWith[(EventPublisher, ZStream[Any, Throwable, EventJournalMessage])](_._2))
    source >>> (publisher ++ stream)
