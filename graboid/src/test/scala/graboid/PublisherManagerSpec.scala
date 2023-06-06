package graboid

import core.KeyGenerator
import graboid.fixture.PublisherFixture
import java.io.IOException
import one.estrondo.sweetmockito.SweetMockito
import one.estrondo.sweetmockito.zio.SweetMockitoLayer
import one.estrondo.sweetmockito.zio.given
import org.mockito.ArgumentMatchers.{eq => eqTo}
import org.mockito.Mockito
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import zio.Scope
import zio.ZIO
import zio.ZLayer
import zio.test.Assertion
import zio.test.TestEnvironment
import zio.test.assert
import zio.test.assertTrue
object PublisherManagerSpec extends Spec:

  override def spec: zio.test.Spec[TestEnvironment & Scope, Any] =
    suite("PublisherManager with mocking:")(
      test("it should insert a valid Publisher into repository.") {
        for
          repository       <- ZIO.service[PublisherRepository]
          validator        <- ZIO.service[PublisherManager.Validator]
          manager          <- ZIO.service[PublisherManager]
          expectedPublisher = PublisherFixture.createRandom()
          _                 = Mockito
                                .when(validator(eqTo(expectedPublisher)))
                                .thenReturn(ZIO.succeed(expectedPublisher))
          _                 = Mockito
                                .when(repository.add(eqTo(expectedPublisher)))
                                .thenReturn(ZIO.succeed(expectedPublisher))
          inserted         <- manager.add(expectedPublisher)
        yield assertTrue(
          inserted == expectedPublisher
        )
      },
      test("it should get a specific Publisher from repository.") {
        val publisher = PublisherFixture.createRandom()
        for
          _       <- SweetMockitoLayer[PublisherRepository]
                       .whenF2(_.get(publisher.key))
                       .thenReturn(Some(publisher))
          manager <- ZIO.service[PublisherManager]
          got     <- manager.get(publisher.key).some
        yield assertTrue(
          got == publisher
        )
      },
      test("it should report any invalid Publisher.") {
        for
          validator             <- ZIO.service[PublisherManager.Validator]
          manager               <- ZIO.service[PublisherManager]
          repository            <- ZIO.service[PublisherRepository]
          expectedPublisher      = PublisherFixture.createRandom()
          expectedCause          = Publisher.Cause("I don't like this Publisher!")
          expectedThrowableCause = GraboidException.Invalid(expectedCause.reason)
          _                      = SweetMockito
                                     .whenF2(validator(eqTo(expectedPublisher)))
                                     .thenFail(
                                       Publisher.Invalid(expectedPublisher, Seq(expectedCause))
                                     )
          exit                  <- manager.add(expectedPublisher).exit
        yield assert(exit)(
          Assertion.fails(
            Assertion.isSubtype[GraboidException.IllegalRequest](Assertion.anything)
          )
        )
      },
      test("it should not insert into repository when validator fails.") {
        for
          validator             <- ZIO.service[PublisherManager.Validator]
          manager               <- ZIO.service[PublisherManager]
          repository            <- ZIO.service[PublisherRepository]
          expectedPublisher      = PublisherFixture.createRandom()
          expectedCause          = Publisher.Cause("I don't like this Publisher!")
          expectedThrowableCause = GraboidException.Invalid(expectedCause.reason)
          _                      = SweetMockito
                                     .whenF2(validator(eqTo(expectedPublisher)))
                                     .thenFail(
                                       Publisher.Invalid(expectedPublisher, Seq(expectedCause))
                                     )
          exit                  <- manager.add(expectedPublisher).exit
        yield assertTrue(
          verify(repository, never()).add(expectedPublisher) == null
        )
      },
      test("it should report any repository failure.") {
        for
          validator        <- ZIO.service[PublisherManager.Validator]
          manager          <- ZIO.service[PublisherManager]
          repository       <- ZIO.service[PublisherRepository]
          expectedThrowable = IllegalStateException("!!!")
          expectedPublisher = PublisherFixture.createRandom()
          _                 = SweetMockito
                                .whenF2(validator(eqTo(expectedPublisher)))
                                .thenReturn(expectedPublisher)
          _                 = SweetMockito
                                .whenF2(repository.add(eqTo(expectedPublisher)))
                                .thenFail(expectedThrowable)
          exit             <- manager.add(expectedPublisher).exit
        yield assert(exit)(
          Assertion.fails(Assertion.hasThrowableCause(Assertion.equalTo(expectedThrowable)))
        )
      },
      test("it should remove a publisher from repository.") {
        val expectedPublisher = PublisherFixture.createRandom()
        val expectedKey       = expectedPublisher.key

        for
          _       <- SweetMockitoLayer[PublisherRepository]
                       .whenF2(_.remove(expectedKey))
                       .thenReturn(Option(expectedPublisher))
          manager <- ZIO.service[PublisherManager]
          result  <- manager.remove(expectedKey)
        yield assertTrue(
          result == Some(expectedPublisher)
        )
      },
      test("it should report any repository error during publisher removing.") {
        val expectedKey   = KeyGenerator.next64()
        val expectedCause = IOException("%%%")
        for
          _       <- SweetMockitoLayer[PublisherRepository]
                       .whenF2(_.remove(expectedKey))
                       .thenFail(expectedCause)
          manager <- ZIO.service[PublisherManager]
          exit    <- manager.remove(expectedKey).exit
        yield assert(exit)(
          Assertion.fails(Assertion.hasThrowableCause(Assertion.equalTo(expectedCause)))
        )
      },
      test("it should update a publisher from repository.") {
        val publisher = PublisherFixture.createRandom()
        val update    = PublisherFixture.updateFrom(publisher)
        val key       = publisher.key

        for
          _       <- SweetMockitoLayer[PublisherRepository]
                       .whenF2(_.update(key, update))
                       .thenReturn(Option(publisher))
          manager <- ZIO.service[PublisherManager]
          result  <- manager.update(key, update)
        yield assertTrue(
          result == Some(publisher)
        )
      },
      test("it should report any repository error during publisher updating.") {
        val expectedCause = IOException("%%%")
        val publisher     = PublisherFixture.createRandom()
        val update        = PublisherFixture.updateFrom(publisher)
        for
          _       <- SweetMockitoLayer[PublisherRepository]
                       .whenF2(_.update(publisher.key, update))
                       .thenFail(expectedCause)
          manager <- ZIO.service[PublisherManager]
          exit    <- manager.update(publisher.key, update).exit
        yield assert(exit)(
          Assertion.fails(Assertion.hasThrowableCause(Assertion.equalTo(expectedCause)))
        )
      }
    ).provideLayer(
      RepositoryMockLayer ++ ValidatorMockLayer ++ ((RepositoryMockLayer ++ ValidatorMockLayer) >>> ManagerLayer)
    )

  private val RepositoryMockLayer =
    ZLayer.succeed(Mockito.mock(classOf[PublisherRepository]))

  private val ValidatorMockLayer =
    ZLayer.succeed(Mockito.mock(classOf[PublisherManager.Validator]))

  private val ManagerLayer = ZLayer {
    for
      repository <- ZIO.service[PublisherRepository]
      validator  <- ZIO.service[PublisherManager.Validator]
    yield PublisherManager(repository, validator)
  }
