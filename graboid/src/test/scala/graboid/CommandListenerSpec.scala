package graboid

// import com.dimafeng.testcontainers.KafkaContainer
import graboid.protocol.CommandDescriptor
import graboid.protocol.CommandExecution
import io.bullet.borer.Cbor
import org.apache.kafka.clients.producer.ProducerRecord
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import graboid.command.AddCrawlerFixture
import graboid.command.RemoveCrawlerFixture
import graboid.command.UpdateCrawlerFixture
import graboid.command.given
import zio.ZIO
import zio.durationInt
import zio.kafka.producer.Producer
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
          ZIO.succeed(CommandExecution(0L, invocation.getArgument[CommandDescriptor](0)))
        }

      val commands = (1 to 10).flatMap { idx =>
        Seq(
          AddCrawlerFixture.createRandom(),
          RemoveCrawlerFixture.createRandom(),
          UpdateCrawlerFixture.createRandom()
        )
      }

      val records =
        for (command, idx) <- commands.zipWithIndex
        yield ProducerRecord(
          "tremors.graboid-command",
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
