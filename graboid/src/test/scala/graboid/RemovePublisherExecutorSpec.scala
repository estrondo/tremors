package graboid

import graboid.command.RemovePublisherExecutor
import graboid.command.RemovePublisherExecutorImpl
import graboid.fixture.PublisherFixture
import graboid.mock.PublisherManagerLayer
import graboid.protocol.GraboidCommandResult
import one.estrondo.sweetmockito.zio.SweetMockitoLayer
import one.estrondo.sweetmockito.zio.given
import testkit.graboid.protocol.RemovePublisherFixture
import zio.Scope
import zio.ZIO
import zio.ZLayer
import zio.test.TestAspect
import zio.test.TestEnvironment
import zio.test.assertTrue

object RemovePublisherExecutorSpec extends Spec:

  override def spec: zio.test.Spec[TestEnvironment & Scope, Any] =
    suite("RemovePublisherCommandExecutor with mocking")(
      test("It should remove a publisher from PublisherManager.") {
        val command   = RemovePublisherFixture.createRandom()
        val publisher = PublisherFixture
          .createRandom()
          .copy(key = command.publisherKey)

        for
          _ <- SweetMockitoLayer[PublisherManager]
                 .whenF2(_.remove(command.publisherKey))
                 .thenReturn(Some(publisher))
          _ <- SweetMockitoLayer[CrawlerExecutor]
                 .whenF2(_.removeExecutions(command.publisherKey))
                 .thenReturn(5L)

          executor <- ZIO.service[RemovePublisherExecutor]
          result   <- executor(command)
        yield assertTrue(
          result == GraboidCommandResult(
            id = command.id,
            time = result.time,
            status = GraboidCommandResult.ok(
              "Publisher has removed.",
              "publisherKey" -> command.publisherKey,
              "executions"   -> "5"
            )
          )
        )
      },
      test("It should return no publisherKey when this key was not found.") {
        val command = RemovePublisherFixture.createRandom()

        for
          _ <- SweetMockitoLayer[PublisherManager]
                 .whenF2(_.remove(command.publisherKey))
                 .thenReturn(None)
          - <- SweetMockitoLayer[CrawlerExecutor]
                 .whenF2(_.removeExecutions(command.publisherKey))
                 .thenReturn(1L)

          executor <- ZIO.service[RemovePublisherExecutor]
          result   <- executor(command)
        yield assertTrue(
          result == GraboidCommandResult(
            id = command.id,
            time = result.time,
            status = GraboidCommandResult.ok(
              "Publisher has removed.",
              "publisherKey" -> command.publisherKey,
              "executions"   -> "0"
            )
          )
        )
      },
      test("It should ignore any failure in CrawlerExecutor.removeExecutions.") {
        val publisher = PublisherFixture.createRandom()
        val command   = RemovePublisherFixture.createRandom()
        val failure   = IllegalStateException("$$$")

        for
          _ <- SweetMockitoLayer[PublisherManager]
                 .whenF2(_.remove(command.publisherKey))
                 .thenReturn(Some(publisher))
          - <- SweetMockitoLayer[CrawlerExecutor]
                 .whenF2(_.removeExecutions(command.publisherKey))
                 .thenFail(failure)

          executor <- ZIO.service[RemovePublisherExecutor]
          result   <- executor(command)
        yield assertTrue(
          result == GraboidCommandResult(
            id = command.id,
            time = result.time,
            status = GraboidCommandResult.ok(
              "Publisher has removed.",
              "publisherKey" -> command.publisherKey,
              "executions"   -> "0"
            )
          )
        )
      }
    ).provideSome(
      PublisherManagerLayer,
      CrawlerExecutorLayer,
      RemovePublisherExecutorLayer
    )

  private val RemovePublisherExecutorLayer = ZLayer {
    for
      manager  <- ZIO.service[PublisherManager]
      executor <- ZIO.service[CrawlerExecutor]
    yield RemovePublisherExecutorImpl(manager, executor)
  }

  private val CrawlerExecutorLayer = SweetMockitoLayer.newMockLayer[CrawlerExecutor]
