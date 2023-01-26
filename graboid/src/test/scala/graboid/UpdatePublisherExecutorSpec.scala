package graboid

import graboid.command.UpdatePublisherExecutor
import graboid.command.UpdatePublisherExecutorImpl
import graboid.fixture.PublisherFixture
import graboid.mock.PublisherManagerLayer
import graboid.protocol.GraboidCommandResult
import testkit.graboid.protocol.UpdatePublisherFixture
import zio.Scope
import zio.ZIO
import zio.ZLayer
import zio.test.TestEnvironment
import zio.test.assertTrue
import zio.test.laws.ZLaws
import one.estrondo.sweetmockito.zio.SweetMockitoLayer
import one.estrondo.sweetmockito.zio.given

object UpdatePublisherExecutorSpec extends Spec:

  override def spec: zio.test.Spec[TestEnvironment & Scope, Any] =
    suite("UpdatePublisherExecutor with mocking")(
      test("it should update a stored publisher in PublisherManager.") {
        val command   = UpdatePublisherFixture.createRandom()
        val update    = PublisherFixture.updateFrom(command.descriptor)
        val publisher = PublisherFixture.from(command.descriptor)

        for
          _        <- SweetMockitoLayer[PublisherManager]
                        .whenF2(_.update(command.descriptor.key, update))
                        .thenReturn(
                          Some(publisher)
                        )
          executor <- ZIO.service[UpdatePublisherExecutor]
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
        val command = UpdatePublisherFixture.createRandom()
        val update  = PublisherFixture.updateFrom(command.descriptor)

        for
          _        <- SweetMockitoLayer[PublisherManager]
                        .whenF2(_.update(command.descriptor.key, update))
                        .thenReturn(None)
          executor <- ZIO.service[UpdatePublisherExecutor]
          result   <- executor(command)
        yield assertTrue(
          result == GraboidCommandResult(
            id = command.id,
            time = result.time,
            status = GraboidCommandResult.Ok(s"Publisher()")
          )
        )
      }
    ).provideSomeLayer(
      PublisherManagerLayer ++ (PublisherManagerLayer >>> UpdatePublisherExecutorLayer)
    )

  private val UpdatePublisherExecutorLayer = ZLayer {
    for manager <- ZIO.service[PublisherManager]
    yield UpdatePublisherExecutorImpl(manager)
  }
