package zkafka

import io.bullet.borer.Cbor
import io.bullet.borer.Codec
import io.bullet.borer.Encoder
import io.bullet.borer.derivation.MapBasedCodecs.deriveCodec
import one.estrondo.sweetmockito.SweetMockito
import one.estrondo.sweetmockito.zio.SweetMockitoLayer
import one.estrondo.sweetmockito.zio.given
import testkit.zio.testcontainers.KafkaLayer
import zio.Scope
import zio.ZIO
import zio.ZLayer
import zio.given
import zio.stream.ZSink
import zio.test.TestAspect
import zio.test.TestClock
import zio.test.TestEnvironment
import zio.test.assertTrue

object KafkaManagerIT extends IT:

  case class Input(content: String, number: Double)

  class Medium(val content: String, val number: Double)

  case class Output(content: String, number: Double)

  trait Subscriber extends KafkaSubscriber[Input, Medium]

  trait Producer extends KafkaProducer[Medium, Output]

  given Codec[Input]  = deriveCodec
  given Codec[Output] = deriveCodec

  def spec: zio.test.Spec[TestEnvironment & Scope, Any] =
    suite("KafkaManager IT.")(
      suite("It with Kafka's Container")(
        test("Given a KafkaSubscriber and KafkaProducer, it should receive a message and publish it.") {
          val input          = Input("The X-Files", 13)
          val expectedMedium = Medium("The X-Files", 26)
          val expected       = Output("The X-Files", 52)
          val bytes          = Cbor.encode(input).toByteArray

          for
            subscriber      <- SweetMockitoLayer[Subscriber]
                                 .whenF2(_.accept("k9", input))
                                 .thenReturn(Some(expectedMedium))
            producer        <- SweetMockitoLayer[Producer]
                                 .whenF2(_.accept("k9", expectedMedium))
                                 .thenReturn(Seq(KafkaMessage(expected, Some("k10"), "imdb")))
            manager         <- ZIO.service[KafkaManager]
            subscribeStream <- manager.subscribe("series", subscriber, producer)
            _               <- KafkaLayer.send("k9", bytes, "series")
            responseStream  <- KafkaLayer.consume("imdb")
            fiber1          <- subscribeStream.run(ZSink.head).some.fork
            fiber2          <- responseStream.run(ZSink.head).some.fork
            _               <- TestClock.adjust(2.seconds)
            record          <- fiber2.join
            sent            <- fiber1.join
          yield
            val (medium, Seq(produced)) = sent
            val result                  = Cbor.decode(record.value()).to[Output].value
            assertTrue(
              result == expected,
              produced == expected,
              medium == expectedMedium
            )

        }
      ).provideSomeLayer(
        SubscriberLayerMockLayer ++ ProducerLayerMockLayer ++ ManagerLayer
      ) @@ TestAspect.sequential
    )

  private val SubscriberLayerMockLayer = ZLayer.succeed(SweetMockito[Subscriber])

  private val ProducerLayerMockLayer = ZLayer.succeed(SweetMockito[Producer])

  private val KafkaConsumerLayer = KafkaLayer.createConsumerLayer("test")

  private val KafkaProducerLayer = KafkaLayer.producerLayer

  private val kafkaLayer = KafkaLayer.layer >+> KafkaConsumerLayer >+> KafkaProducerLayer

  private val ManagerLayer = kafkaLayer >+> ZLayer {
    for
      producerSettings <- KafkaLayer.producerSettings()
      consumerSettings <- KafkaLayer.consumerSettings("test")
    yield KafkaManager(consumerSettings, producerSettings)
  }
