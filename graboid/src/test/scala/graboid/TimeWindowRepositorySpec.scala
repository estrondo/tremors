package graboid

import farango.FarangoDocumentCollection
import graboid.fixture.TimeWindowFixture
import graboid.mock.FarangoDocumentCollectionMockLayer
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.{eq => eqTo}
import org.mockito.Mockito
import org.mockito.Mockito.verify
import zio.Scope
import zio.ZIO
import zio.ZLayer
import zio.test.Assertion
import zio.test.TestEnvironment
import zio.test.assert
import zio.test.assertTrue
import zio.test.assertZIO
import testkit.core.SweetMockito

object TimeWindowRepositorySpec extends Spec:

  override def spec: zio.test.Spec[TestEnvironment & Scope, Any] =
    suite("TimelineRepository with mocking")(
      test("it should reporty any Arango failure.") {
        for
          collection        <- ZIO.service[FarangoDocumentCollection]
          repository        <- ZIO.service[TimeWindowRepository]
          expectedTimeWindow = TimeWindowFixture.createRandom()
          expectedThrowable  = IllegalStateException("@@@")
          expectedDocument   = TimeWindowRepository
                                 .timeWindowToDocument(expectedTimeWindow)
          _                  = SweetMockito
                                 .failF(collection.insert(eqTo(expectedDocument))(any(), any()))(expectedThrowable)
          exit              <- repository.add(expectedTimeWindow).exit
        yield assert(exit)(
          Assertion.fails(
            Assertion.hasThrowableCause(Assertion.equalTo(expectedThrowable)) && Assertion
              .isSubtype[GraboidException](Assertion.anything)
          )
        )
      }
    ).provideLayer(MockLayer)

  val RepositoryMockLayer = ZLayer {
    for collection <- ZIO.service[FarangoDocumentCollection]
    yield TimeWindowRepository(collection)
  }

  val MockLayer =
    FarangoDocumentCollectionMockLayer ++ (FarangoDocumentCollectionMockLayer >>> RepositoryMockLayer)
