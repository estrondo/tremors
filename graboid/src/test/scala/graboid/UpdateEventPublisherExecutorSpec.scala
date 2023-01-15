package graboid

import graboid.command.UpdateEventPublisherExecutor
import graboid.command.UpdateEventPublisherExecutorImpl
import graboid.fixture.EventPublisherFixture
import graboid.mock.EventPublisherManagerLayer
import graboid.protocol.GraboidCommandResult
import testkit.graboid.protocol.UpdateEventPublisherFixture
import zio.Scope
import zio.ZIO
import zio.ZLayer
import zio.test.TestEnvironment
import zio.test.assertTrue
import zio.test.laws.ZLaws

object UpdateEventPublisherExecutorSpec extends Spec:

  override def spec: zio.test.Spec[TestEnvironment & Scope, Any] =
    suite("UpdateEventPublisherExecutor with mocking")(
      test("it should update a stored publisher in EventPublisherManager.") {
        val command   = UpdateEventPublisherFixture.createRandom()
        val update    = EventPublisherFixture.updateFrom(command.descriptor)
        val publisher = EventPublisherFixture.from(command.descriptor)

        for
          _        <- sweetMock[EventPublisherManager].returnF(_.update(command.descriptor.key, update))(
                        Some(publisher)
                      )
          executor <- ZIO.service[UpdateEventPublisherExecutor]
          result   <- executor(command)
        yield assertTrue(
          result == GraboidCommandResult(
            id = command.id,
            time = result.time,
            status = GraboidCommandResult.Ok(s"EventPublisher(${publisher.key})")
          )
        )
      },
      test("it should return no publisherKey when this key was not found.") {
        val command = UpdateEventPublisherFixture.createRandom()
        val update  = EventPublisherFixture.updateFrom(command.descriptor)

        for
          _        <- sweetMock[EventPublisherManager]
                        .returnF(_.update(command.descriptor.key, update))(None)
          executor <- ZIO.service[UpdateEventPublisherExecutor]
          result   <- executor(command)
        yield assertTrue(
          result == GraboidCommandResult(
            id = command.id,
            time = result.time,
            status = GraboidCommandResult.Ok(s"EventPublisher()")
          )
        )
      }
    ).provideSomeLayer(
      EventPublisherManagerLayer ++ (EventPublisherManagerLayer >>> UpdateEventPublisherExecutorLayer)
    )

  private val UpdateEventPublisherExecutorLayer = ZLayer {
    for manager <- ZIO.service[EventPublisherManager]
    yield UpdateEventPublisherExecutorImpl(manager)
  }
