package graboid

import farango.DocumentCollection
import graboid.fixture.EventRecordFixture
import graboid.mock.FarangoDocumentCollectionMockLayer
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.{eq => eqTo}
import org.mockito.Mockito
import zio.Scope
import zio.ZIO
import zio.ZLayer
import zio.test.Assertion
import zio.test.TestEnvironment
import zio.test.assert
import zio.test.assertTrue

import java.io.IOException
import one.estrondo.sweetmockito.SweetMockito
import one.estrondo.sweetmockito.zio.given
import farango.DocumentCollection
import farango.zio.ZEffect
import zio.test.TestAspect

object EventRecordRepositorySpec extends Spec:

  override def spec: zio.test.Spec[TestEnvironment & Scope, Any] =
    suite("EventRecordRepository with mocking")(
      test("should report any Arango failure.") {
        for
          collection         <- ZIO.service[DocumentCollection]
          repository         <- ZIO.service[EventRecordRepository]
          expectedEventRecord = EventRecordFixture.createRandom()
          expectedThrowable   = IOException("@@@@")
          expectedDocument    = EventRecordRepository
                                  .given_Conversion_EventRecord_Document(expectedEventRecord)
          _                   = SweetMockito
                                  .whenF2(
                                    collection.insert[EventRecordRepository.Document, ZEffect](eqTo(expectedDocument))(any(), any())
                                  )
                                  .thenFail(expectedThrowable)
          exit               <- repository.add(expectedEventRecord).exit
        yield assert(exit)(
          Assertion.fails(Assertion.hasThrowableCause(Assertion.equalTo(expectedThrowable)))
        )
      } @@ TestAspect.ignore
    ).provideLayer(MockLayer)

  val RepositoryMockLayer = ZLayer {
    for collection <- ZIO.service[DocumentCollection]
    yield EventRecordRepository(collection)
  }

  val MockLayer =
    FarangoDocumentCollectionMockLayer ++ (FarangoDocumentCollectionMockLayer >>> RepositoryMockLayer)
