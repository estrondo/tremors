package graboid

import farango.DocumentCollection
import graboid.fixture.TimeWindowFixture
import graboid.mock.FarangoDocumentCollectionMockLayer
import one.estrondo.sweetmockito.SweetMockito
import one.estrondo.sweetmockito.zio.given
import one.estrondo.sweetmockito.zio.SweetMockitoLayer
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
import farango.zio.{ZEffect, given}
import farango.PartialDocumentInsert
import zio.test.TestAspect

object TimeWindowRepositorySpec extends Spec:

  override def spec: zio.test.Spec[TestEnvironment & Scope, Any] =
    suite("TimelineRepository with mocking")(
      test("it should reporty any Arango failure.") {

        for
          repository        <- ZIO.service[TimeWindowRepository]
          collection        <- ZIO.service[DocumentCollection]
          documentInsertMock = SweetMockito[PartialDocumentInsert[TimeWindowRepository.Document]]
          expectedTimeWindow = TimeWindowFixture.createRandom()
          expectedThrowable  = IllegalStateException("@@@")
          expectedDocument   = TimeWindowRepository
                                 .timeWindowToDocument(expectedTimeWindow)
          _                  =
            SweetMockito
              .whenF2(
                documentInsertMock[TimeWindowRepository.Document, ZEffect](eqTo(expectedTimeWindow))(
                  any(),
                  any(),
                  any()
                )
              )
              .thenFail(expectedThrowable)

          _ = Mockito
                .when(collection.insertT[TimeWindowRepository.Document])
                .thenReturn(documentInsertMock)

          exit <- repository.add(expectedTimeWindow).exit
        yield assert(exit)(
          Assertion.fails(
            Assertion.hasThrowableCause(Assertion.equalTo(expectedThrowable)) && Assertion
              .isSubtype[GraboidException](Assertion.anything)
          )
        )
      } @@ TestAspect.ignore
    ).provideLayer(MockLayer)

  val RepositoryMockLayer = ZLayer {
    for collection <- ZIO.service[DocumentCollection]
    yield TimeWindowRepository(collection)
  }

  val MockLayer =
    FarangoDocumentCollectionMockLayer ++ (FarangoDocumentCollectionMockLayer >>> RepositoryMockLayer)
