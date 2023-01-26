package graboid

import farango.DocumentCollection
import graboid.fixture.EventPublisherFixture
import graboid.mock.FarangoDocumentCollectionMockLayer
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.{eq => eqTo}
import org.mockito.Mockito
import zio.Runtime
import zio.Scope
import zio.ZIO
import zio.ZLayer
import zio.logging.backend.SLF4J
import zio.test.Assertion
import zio.test.TestEnvironment
import zio.test.assertTrue
import zio.test.assertZIO
import one.estrondo.sweetmockito.SweetMockito
import one.estrondo.sweetmockito.zio.given
import farango.DocumentCollection
import farango.zio.ZEffect
import zio.test.TestAspect

object EventPublisherRepositorySpec extends Spec:

  override def spec =
    suite("EventPublisherRepository with mocking")(
      test("should report a GraboidException.Unexpected for any Arango failure.") {
        val effect = for
          collection   <- ZIO.service[DocumentCollection]
          repository   <- ZIO.service[EventPublisherRepository]
          expectedCause = IllegalStateException("!!!")
          _             = SweetMockito
                            .whenF2(collection.insert[EventPublisherRepository.Document, ZEffect](any())(any(), any()))
                            .thenFail(expectedCause)
          inserted     <- repository
                            .add(EventPublisherFixture.createRandom())
        yield inserted

        assertZIO(effect.exit)(
          Assertion.fails(Assertion.isSubtype[GraboidException.Unexpected](Assertion.anything))
        )
      } @@ TestAspect.ignore
    ).provideLayer(MockLayer)

  val RepositoryMockLayer: ZLayer[DocumentCollection, Nothing, EventPublisherRepository] =
    ZLayer {
      for collection <- ZIO.service[DocumentCollection]
      yield EventPublisherRepository(collection)
    }

  val MockLayer =
    FarangoDocumentCollectionMockLayer ++ (FarangoDocumentCollectionMockLayer >>> RepositoryMockLayer)
