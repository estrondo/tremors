package graboid

import farango.DocumentCollection
import farango.zio.ZEffect
import graboid.fixture.PublisherFixture
import graboid.mock.FarangoDocumentCollectionMockLayer
import one.estrondo.sweetmockito.SweetMockito
import one.estrondo.sweetmockito.zio.given
import org.mockito.ArgumentMatchers.{eq => eqTo}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import zio.Runtime
import zio.Scope
import zio.ZIO
import zio.ZLayer
import zio.logging.backend.SLF4J
import zio.test.Assertion
import zio.test.TestAspect
import zio.test.TestEnvironment
import zio.test.assertTrue
import zio.test.assertZIO

object PublisherRepositorySpec extends Spec:

  override def spec =
    suite("PublisherRepository with mocking")(
      test("should report a GraboidException.Unexpected for any Arango failure.") {
        val effect = for
          collection   <- ZIO.service[DocumentCollection]
          repository   <- ZIO.service[PublisherRepository]
          expectedCause = IllegalStateException("!!!")
          _             = SweetMockito
                            .whenF2(
                              collection
                                .insert[PublisherRepository.Document][Publisher, ZEffect](any())(any(), any(), any())
                            )
                            .thenFail(expectedCause)
          inserted     <- repository
                            .add(PublisherFixture.createRandom())
        yield inserted

        assertZIO(effect.exit)(
          Assertion.fails(Assertion.isSubtype[GraboidException.Unexpected](Assertion.anything))
        )
      } @@ TestAspect.ignore
    ).provideLayer(MockLayer)

  val RepositoryMockLayer: ZLayer[DocumentCollection, Nothing, PublisherRepository] =
    ZLayer {
      for collection <- ZIO.service[DocumentCollection]
      yield PublisherRepository(collection)
    }

  val MockLayer =
    FarangoDocumentCollectionMockLayer ++ (FarangoDocumentCollectionMockLayer >>> RepositoryMockLayer)
