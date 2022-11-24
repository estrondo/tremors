package webapi1x.graboid

import com.dimafeng.testcontainers.KafkaContainer
import core.KeyGenerator
import graboid.protocol.AddCrawler
import graboid.protocol.GraboidCommand
import graboid.protocol.GraboidCommandExecution
import io.bullet.borer.Cbor
import testkit.graboid.protocol.CrawlerDescriptorFixture
import testkit.graboid.protocol.RemoveCrawlerFixture
import testkit.graboid.protocol.UpdateCrawlerFixture
import testkit.zio.testcontainers.KafkaContainerLayer
import webapi1x.Spec
import zio.RIO
import zio.RLayer
import zio.Scope
import zio.ZIO
import zio.ZLayer
import zio.durationInt
import zio.kafka.producer.Producer
import zio.stream.ZSink
import zio.test.TestClock
import zio.test.TestEnvironment
import zio.test.TestResult

object GraboidCommandDispatcherSpec extends Spec:

  private def graboidCommandDispatcherLayer: RLayer[Producer, GraboidCommandDispatcher] =
    ZLayer {
      for
        producer       <- ZIO.service[Producer]
        crawlerManager <- GraboidCommandDispatcher(ZLayer.succeed(producer), KeyGenerator)
      yield crawlerManager
    }

  override def spec: zio.test.Spec[TestEnvironment & Scope, Any] =
    suite("GraboidCommandDispatcher")(
      test("should send AddCrawler") {
        checkCommand(AddCrawler(CrawlerDescriptorFixture.createRandom()))
      },
      test("should send UpdateCrawler") {
        checkCommand(UpdateCrawlerFixture.createRandom())
      },
      test("should send RemoveCrawler") {
        checkCommand(RemoveCrawlerFixture.createRandom())
      }
    ).provideSomeLayer(
      KafkaContainerLayer.layer >+> (KafkaContainerLayer.producerLayer >>> graboidCommandDispatcherLayer)
    )

  private def checkCommand[A <: GraboidCommand](
      command: A
  ): RIO[GraboidCommandDispatcher & KafkaContainer & Scope, TestResult] =
    for
      stream     <- KafkaContainerLayer.consume(GraboidCommandDispatcher.GraboidCommandTopic)
      fiber      <- stream.run(ZSink.collectAllN(1)).fork
      dispatcher <- ZIO.service[GraboidCommandDispatcher]
      result     <- dispatcher.dispatch(command)
      _          <- TestClock.adjust(5.seconds)
      records    <- fiber.join
      record      = records.head
      sent       <- ZIO.attempt(Cbor.decode(record.value()).to[GraboidCommand].value)
    yield assertTrue(
      result.command == command,
      record.key() == result.key,
      sent == command
    )
