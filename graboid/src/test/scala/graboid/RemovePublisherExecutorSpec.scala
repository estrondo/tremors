package graboid

import graboid.command.RemovePublisherExecutor
import graboid.command.RemovePublisherExecutorImpl
import graboid.fixture.PublisherFixture
import graboid.mock.PublisherManagerLayer
import graboid.protocol.GraboidCommandResult
import testkit.graboid.protocol.RemovePublisherFixture
import zio.Scope
import zio.ZIO
import zio.ZLayer
import zio.test.TestAspect
import zio.test.TestEnvironment
import zio.test.assertTrue
import one.estrondo.sweetmockito.zio.SweetMockitoLayer
import one.estrondo.sweetmockito.zio.given

object RemovePublisherExecutorSpec extends Spec:

  override def spec: zio.test.Spec[TestEnvironment & Scope, Any] =
    suite("RemovePublisherCommandExecutor with mocking")(
      test("it should remove a publisher from PublisherManager.") {
        val command   = RemovePublisherFixture.createRandom()
        val publisher = PublisherFixture
          .createRandom()
          .copy(key = command.publisherKey)

        for
          _        <- SweetMockitoLayer[PublisherManager]
                        .whenF2(_.remove(command.publisherKey))
                        .thenReturn(Some(publisher))
          executor <- ZIO.service[RemovePublisherExecutor]
          result   <- executor(command)
        yield assertTrue(
          result == GraboidCommandResult(
            id = command.id,
            time = result.time,
            status = GraboidCommandResult.Ok(s"Publisher(${publisher.key})")
          )
        )
      },
      test("it should return no publisherKey when this key was not found.") {
        val command = RemovePublisherFixture.createRandom()

        for
          _        <- SweetMockitoLayer[PublisherManager]
                        .whenF2(_.remove(command.publisherKey))
                        .thenReturn(None)
          executor <- ZIO.service[RemovePublisherExecutor]
          result   <- executor(command)
        yield assertTrue(
          result == GraboidCommandResult(
            id = command.id,
            time = result.time,
            status = GraboidCommandResult.Ok("Publisher()")
          )
        )
      }
    ).provideSomeLayer(
      PublisherManagerLayer ++ (PublisherManagerLayer >>> RemovePublisherExecutorLayer)
    )

  private val RemovePublisherExecutorLayer = ZLayer {
    for manager <- ZIO.service[PublisherManager]
    yield RemovePublisherExecutorImpl(manager)
  }
