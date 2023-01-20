package graboid

import graboid.command.RemoveEventPublisherExecutor
import graboid.command.RemoveEventPublisherExecutorImpl
import graboid.fixture.EventPublisherFixture
import graboid.mock.EventPublisherManagerLayer
import graboid.protocol.GraboidCommandResult
import testkit.graboid.protocol.RemoveEventPublisherFixture
import zio.Scope
import zio.ZIO
import zio.ZLayer
import zio.test.TestAspect
import zio.test.TestEnvironment
import zio.test.assertTrue
import one.estrondo.sweetmockito.zio.SweetMockitoLayer
import one.estrondo.sweetmockito.zio.given

object RemoveEventPublisherExecutorSpec extends Spec:

  override def spec: zio.test.Spec[TestEnvironment & Scope, Any] =
    suite("RemoveEventPublisherCommandExecutor with mocking")(
      test("it should remove a publisher from EventPublisherManager.") {
        val command        = RemoveEventPublisherFixture.createRandom()
        val eventPublisher = EventPublisherFixture
          .createRandom()
          .copy(key = command.publisherKey)

        for
          _        <- SweetMockitoLayer[EventPublisherManager]
                        .whenF2(_.remove(command.publisherKey))
                        .thenReturn(Some(eventPublisher))
          executor <- ZIO.service[RemoveEventPublisherExecutor]
          result   <- executor(command)
        yield assertTrue(
          result == GraboidCommandResult(
            id = command.id,
            time = result.time,
            status = GraboidCommandResult.Ok(s"EventPublisher(${eventPublisher.key})")
          )
        )
      },
      test("it should return no publisherKey when this key was not found.") {
        val command = RemoveEventPublisherFixture.createRandom()

        for
          _        <- SweetMockitoLayer[EventPublisherManager]
                        .whenF2(_.remove(command.publisherKey))
                        .thenReturn(None)
          executor <- ZIO.service[RemoveEventPublisherExecutor]
          result   <- executor(command)
        yield assertTrue(
          result == GraboidCommandResult(
            id = command.id,
            time = result.time,
            status = GraboidCommandResult.Ok("EventPublisher()")
          )
        )
      }
    ).provideSomeLayer(
      EventPublisherManagerLayer ++ (EventPublisherManagerLayer >>> RemoveEventPublisherExecutorLayer)
    )

  private val RemoveEventPublisherExecutorLayer = ZLayer {
    for manager <- ZIO.service[EventPublisherManager]
    yield RemoveEventPublisherExecutorImpl(manager)
  }
