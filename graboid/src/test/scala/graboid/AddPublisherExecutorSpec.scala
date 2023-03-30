package graboid

import graboid.CommandExecutor
import graboid.PublisherManager
import graboid.command.AddPublisherExecutor
import graboid.command.AddPublisherExecutorImpl
import graboid.fixture.PublisherFixture
import graboid.mock.PublisherManagerLayer
import graboid.protocol.GraboidCommandResult
import one.estrondo.sweetmockito.SweetMockito
import one.estrondo.sweetmockito.zio.given
import org.mockito.ArgumentMatchers
import testkit.graboid.protocol.AddPublisherFixture
import testkit.graboid.protocol.GraboidCommandResultFixture
import zio.Scope
import zio.ZIO
import zio.ZLayer
import zio.test.Assertion
import zio.test.TestEnvironment
import zio.test.assert
import zio.test.assertTrue

object AddPublisherExecutorSpec extends Spec:

  override def spec: zio.test.Spec[TestEnvironment & Scope, Any] =
    suite("AddPublisherExecutor with mocking")(
      test("it should insert a new publisher into PublisherManager") {
        val command = AddPublisherFixture
          .createRandom()

        val publisher = PublisherFixture.from(command.descriptor)

        for
          mock     <- ZIO.service[PublisherManager]
          executor <- ZIO.service[AddPublisherExecutor]
          _         = SweetMockito
                        .whenF2(mock.add(publisher))
                        .thenReturn(publisher)
          result   <- executor(command)
        yield assertTrue(
          result == GraboidCommandResult(
            id = command.id,
            time = result.time,
            status = GraboidCommandResult.ok("Publisher added.", "publisherKey" -> publisher.key)
          )
        )
      },
      test("it should catch any PublisherManager error.") {
        val expectedException = IllegalArgumentException("###")
        val command           = AddPublisherFixture.createRandom()
        for
          mock     <- ZIO.service[PublisherManager]
          executor <- ZIO.service[AddPublisherExecutor]
          _         = SweetMockito
                        .whenF2(mock.add(ArgumentMatchers.any()))
                        .thenFail(expectedException)
          result   <- executor(command)
        yield assertTrue(
          result == GraboidCommandResult(
            id = command.id,
            time = result.time,
            status = GraboidCommandResult.failed("An error ocurred!", Seq("###"))
          )
        )
      }
    ).provideSomeLayer(
      PublisherManagerLayer ++ (PublisherManagerLayer >>> AddPublisherExecutorLayer)
    )

  private val AddPublisherExecutorLayer = ZLayer {
    for manager <- ZIO.service[PublisherManager]
    yield AddPublisherExecutorImpl(manager)
  }
