package graboid

import farango.FarangoDocumentCollection
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
import ziorango.given
import testkit.core.SweetMockito

object EventPublisherRepositorySpec extends Spec:

  override def spec =
    suite("EventPublisherRepository with mocking")(
      test("should report a GraboidException.Unexpected for any Arango failure.") {
        val effect = for
          collection   <- ZIO.service[FarangoDocumentCollection]
          repository   <- ZIO.service[EventPublisherRepository]
          expectedCause = IllegalStateException("!!!")
          _             = SweetMockito
                            .failF(collection.insert(any())(any(), any()))(expectedCause)
          inserted     <- repository
                            .add(EventPublisherFixture.createRandom())
        yield inserted

        assertZIO(effect.exit)(
          Assertion.fails(Assertion.isSubtype[GraboidException.Unexpected](Assertion.anything))
        )
      }
    ).provideLayer(MockLayer)

  val RepositoryMockLayer: ZLayer[FarangoDocumentCollection, Nothing, EventPublisherRepository] =
    ZLayer {
      for collection <- ZIO.service[FarangoDocumentCollection]
      yield EventPublisherRepository(collection)
    }

  val MockLayer =
    FarangoDocumentCollectionMockLayer ++ (FarangoDocumentCollectionMockLayer >>> RepositoryMockLayer)
