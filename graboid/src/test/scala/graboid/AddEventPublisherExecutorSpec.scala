package graboid

import graboid.EventPublisherManager
import graboid.command.AddEventPublisherExecutor
import graboid.command.AddEventPublisherExecutorImpl
import graboid.CommandExecutor
import graboid.fixture.EventPublisherFixture
import graboid.mock.EventPublisherManagerLayer
import graboid.protocol.GraboidCommandResult
import one.estrondo.sweetmockito.SweetMockito
import one.estrondo.sweetmockito.zio.given
import org.mockito.ArgumentMatchers
import testkit.graboid.protocol.AddEventPublisherFixture
import testkit.graboid.protocol.GraboidCommandResultFixture
import zio.Scope
import zio.ZIO
import zio.ZLayer
import zio.test.Assertion
import zio.test.TestEnvironment
import zio.test.assert
import zio.test.assertTrue

object AddEventPublisherExecutorSpec extends Spec:

  override def spec: zio.test.Spec[TestEnvironment & Scope, Any] =
    suite("AddEventPublisherExecutor with mocking")(
      test("it should insert a new publisher into EventPublisherManager") {
        val command = AddEventPublisherFixture
          .createRandom()

        val eventPublisher = EventPublisherFixture.from(command.descriptor)

        for
          mock     <- ZIO.service[EventPublisherManager]
          executor <- ZIO.service[AddEventPublisherExecutor]
          _         = SweetMockito
                        .whenF2(mock.add(eventPublisher))
                        .thenReturn(eventPublisher)
          result   <- executor(command)
        yield assertTrue(
          result == GraboidCommandResult(
            id = command.id,
            time = result.time,
            status = GraboidCommandResult.Ok(s"EventPublisher(${eventPublisher.key})")
          )
        )
      },
      test("it should catch any EventPublisherManager error.") {
        val expectedException = IllegalArgumentException("###")
        val command           = AddEventPublisherFixture.createRandom()
        for
          mock     <- ZIO.service[EventPublisherManager]
          executor <- ZIO.service[AddEventPublisherExecutor]
          _         = SweetMockito
                        .whenF2(mock.add(ArgumentMatchers.any()))
                        .thenFail(expectedException)
          result   <- executor(command)
        yield assertTrue(
          result == GraboidCommandResult(
            id = command.id,
            time = result.time,
            status = GraboidCommandResult.Failed(Seq("java.lang.IllegalArgumentException: ###"))
          )
        )
      }
    ).provideSomeLayer(
      EventPublisherManagerLayer ++ (EventPublisherManagerLayer >>> AddEventPublisherExecutorLayer)
    )

  private val AddEventPublisherExecutorLayer = ZLayer {
    for manager <- ZIO.service[EventPublisherManager]
    yield AddEventPublisherExecutorImpl(manager)
  }
