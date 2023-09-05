package tremors.zio.kafka

import com.dimafeng.testcontainers.KafkaContainer
import java.util.UUID
import one.estrondo.sweetmockito.SweetMockito
import one.estrondo.sweetmockito.zio.given
import tremors.zio.kafka.cbor.Borer
import zio.Scope
import zio.ZIO
import zio.ZLayer
import zio.durationInt
import zio.kafka.consumer.Consumer
import zio.kafka.consumer.Subscription
import zio.kafka.producer.Producer
import zio.kafka.serde.Serde
import zio.stream.ZSink
import zio.stream.ZStream
import zio.test.TestAspect
import zio.test.TestClock
import zio.test.assertTrue

object KafkaRouterSpec extends ZIOKafkaSpec:

  given KWriter[Input]  = Borer.writer
  given KReader[Input]  = Borer.reader
  given KWriter[Output] = Borer.writer
  given KReader[Output] = Borer.reader

  def spec = suite("KafkaRouterSpec")(
    test("It should read and produce messages.") {
      val consumerMock   = SweetMockito[(String, Input) => ZStream[Any, Throwable, Middle]]
      val producerMock   = SweetMockito[Middle => ZStream[Any, Throwable, (String, String, Output)]]
      val expectedInput  = Input()
      val expectedMiddle = Middle(expectedInput.id, expectedInput.key)
      val expectedOutput = Output(expectedInput.id, expectedInput.key)

      SweetMockito
        .whenF2(consumerMock("a-key", expectedInput))
        .thenReturn(expectedMiddle)

      SweetMockito
        .whenF2(producerMock(expectedMiddle))
        .thenReturn(("output-topic", "a-key", expectedOutput))

      val consumer = KConsumer("input-topic", consumerMock)
      val producer = KProducer.Custom(producerMock)

      for
        bytes             <- summon[KWriter[Input]](expectedInput)
        routerResultRef   <- ZIO.serviceWithZIO[KafkaRouter](_.subscribe(consumer, producer).run(ZSink.head)).fork
        _                 <- Producer.produce("input-topic", "a-key", bytes, Serde.string, Serde.byteArray)
        topicResultRef    <- Consumer
                               .plainStream(Subscription.topics("output-topic"), Serde.string, Serde.byteArray)
                               .runHead
                               .fork
        _                 <- TestClock.adjust(1.second)
        routerResult      <- routerResultRef.join
        topicResultRecord <- topicResultRef.join
        topicResult       <- summon[KReader[Output]](topicResultRecord.get.value)
      yield assertTrue(
        routerResult.contains(expectedOutput),
        topicResult == expectedOutput
      )
    }
  ).provideSome[Scope](
    kafkaContainer,
    kafkaProducer,
    kafkaConsumer,
    ZLayer {
      ZIO.serviceWithZIO[KafkaContainer](kafka =>
        KafkaRouter(
          "test-client",
          KafkaConfig(
            consumer = KafkaConsumerConfig(
              groupId = "test-group",
              bootstrapServers = List(kafka.bootstrapServers)
            ),
            producer = KafkaProducerConfig(
              bootstrapServers = List(kafka.bootstrapServers)
            )
          )
        )
      )
    }
  ) @@ TestAspect.sequential

  case class Input(id: String = UUID.randomUUID().toString, key: String = UUID.randomUUID().toString)

  case class Middle(id: String, key: String)

  case class Output(id: String, key: String)
