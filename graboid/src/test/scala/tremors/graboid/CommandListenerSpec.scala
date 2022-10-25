package tremors.graboid

import com.dimafeng.testcontainers.KafkaContainer
import io.bullet.borer.Cbor
import org.apache.kafka.clients.producer.ProducerRecord
import org.mockito.Answers
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.testcontainers.utility.DockerImageName
import tremors.graboid.command.*
import tremors.graboid.command.given
import tremors.ziotestcontainers.*
import tremors.ziotestcontainers.given
import zio.RIO
import zio.Task
import zio.TaskLayer
import zio.ZIO
import zio.ZLayer
import zio.durationInt
import zio.kafka.consumer.Consumer
import zio.kafka.consumer.ConsumerSettings
import zio.kafka.producer.Producer
import zio.kafka.producer.ProducerSettings
import zio.kafka.serde.Serde
import zio.stream.ZSink
import zio.stream.ZStream
import zio.test.TestClock
import zio.test.assertTrue

object CommandListenerSpec extends Spec:

  override def spec = suite("A CommandListener")(
    test("should receive some commands from Kafka") {
      val executor = Mockito.mock(classOf[CommandExecutor])
      val listener = CommandListener(executor)

      Mockito
        .when(executor(ArgumentMatchers.any(classOf[CommandDescriptor])))
        .thenAnswer { invocation =>
          ZIO.succeed(CommandExecution(invocation.getArgument[CommandDescriptor](0)))
        }

      val commands = (1 to 10).flatMap { idx =>
        Seq(
          AddCrawler(name = s"crawler-$idx"),
          RemoveCrawler(name = s"crawler-$idx"),
          UpdateCrawler(name = s"crawler-$idx")
        )
      }

      val records =
        for (command, idx) <- commands.zipWithIndex
        yield ProducerRecord(
          CommandListener.Topic,
          s"k-$idx",
          Cbor.encode(command).toByteArray
        )

      for
        consumerLayer <- KafkaContainerLayer.createConsumerLayer(CommandListener.Group)
        fiber         <- listener
                           .run()
                           .provideLayer(consumerLayer)
                           .run(ZSink.collectAllN(records.size))
                           .fork
        producerLayer <- KafkaContainerLayer.createProducerLayer()
        _             <- ZStream
                           .fromIterable(records)
                           .mapZIO(Producer.produce(_, Serde.string, Serde.byteArray))
                           .run(ZSink.drain)
                           .provideLayer(producerLayer)
        _             <- TestClock.adjust(5.seconds)
        executions    <- fiber.join
      yield assertTrue(executions.size == records.size)
    }.provideLayer(KafkaContainerLayer.layer)
  )
