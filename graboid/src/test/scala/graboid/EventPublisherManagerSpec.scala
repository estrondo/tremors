package graboid

import cats.syntax.validated
import graboid.fixture.EventPublisherFixture
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.{eq => eqTo}
import org.mockito.Mock
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
import core.KeyGenerator
import java.io.IOException
import one.estrondo.sweetmockito.zio.SweetMockitoLayer
import one.estrondo.sweetmockito.zio.given
import one.estrondo.sweetmockito.SweetMockito

object EventPublisherManagerSpec extends Spec:

  override def spec: zio.test.Spec[TestEnvironment & Scope, Any] =
    suite("EventPublisherManager with mocking:")(
      test("it should insert a valid EventPublisher into repository.") {
        for
          repository       <- ZIO.service[EventPublisherRepository]
          validator        <- ZIO.service[EventPublisherManager.Validator]
          manager          <- ZIO.service[EventPublisherManager]
          expectedPublisher = EventPublisherFixture.createRandom()
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
      test("it should report any invalid EventPublisher.") {
        for
          validator             <- ZIO.service[EventPublisherManager.Validator]
          manager               <- ZIO.service[EventPublisherManager]
          repository            <- ZIO.service[EventPublisherRepository]
          expectedPublisher      = EventPublisherFixture.createRandom()
          expectedCause          = EventPublisher.Cause("I don't like this Publisher!")
          expectedThrowableCause = GraboidException.Invalid(expectedCause.reason)
          _                      = SweetMockito
                                     .whenF2(validator(eqTo(expectedPublisher)))
                                     .thenFail(
                                       EventPublisher.Invalid(expectedPublisher, Seq(expectedCause))
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
          validator             <- ZIO.service[EventPublisherManager.Validator]
          manager               <- ZIO.service[EventPublisherManager]
          repository            <- ZIO.service[EventPublisherRepository]
          expectedPublisher      = EventPublisherFixture.createRandom()
          expectedCause          = EventPublisher.Cause("I don't like this Publisher!")
          expectedThrowableCause = GraboidException.Invalid(expectedCause.reason)
          _                      = SweetMockito
                                     .whenF2(validator(eqTo(expectedPublisher)))
                                     .thenFail(
                                       EventPublisher.Invalid(expectedPublisher, Seq(expectedCause))
                                     )
          exit                  <- manager.add(expectedPublisher).exit
        yield assertTrue(
          verify(repository, never()).add(expectedPublisher) == null
        )
      },
      test("it should report any repository failure.") {
        for
          validator        <- ZIO.service[EventPublisherManager.Validator]
          manager          <- ZIO.service[EventPublisherManager]
          repository       <- ZIO.service[EventPublisherRepository]
          expectedThrowable = IllegalStateException("!!!")
          expectedPublisher = EventPublisherFixture.createRandom()
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
        val expectedPublisher = EventPublisherFixture.createRandom()
        val expectedKey       = expectedPublisher.key

        for
          _       <- SweetMockitoLayer[EventPublisherRepository]
                       .whenF2(_.remove(expectedKey))
                       .thenReturn(Option(expectedPublisher))
          manager <- ZIO.service[EventPublisherManager]
          result  <- manager.remove(expectedKey)
        yield assertTrue(
          result == Some(expectedPublisher)
        )
      },
      test("it should report any repository error during publisher removing.") {
        val expectedKey   = KeyGenerator.next64()
        val expectedCause = IOException("%%%")
        for
          _       <- SweetMockitoLayer[EventPublisherRepository]
                       .whenF2(_.remove(expectedKey))
                       .thenFail(expectedCause)
          manager <- ZIO.service[EventPublisherManager]
          exit    <- manager.remove(expectedKey).exit
        yield assert(exit)(
          Assertion.fails(Assertion.hasThrowableCause(Assertion.equalTo(expectedCause)))
        )
      },
      test("it should update a publisher from repository.") {
        val publisher = EventPublisherFixture.createRandom()
        val update    = EventPublisherFixture.updateFrom(publisher)
        val key       = publisher.key

        for
          _       <- SweetMockitoLayer[EventPublisherRepository]
                       .whenF2(_.update(key, update))
                       .thenReturn(Option(publisher))
          manager <- ZIO.service[EventPublisherManager]
          result  <- manager.update(key, update)
        yield assertTrue(
          result == Some(publisher)
        )
      },
      test("it should report any repository error during publisher updating.") {
        val expectedCause = IOException("%%%")
        val publisher     = EventPublisherFixture.createRandom()
        val update        = EventPublisherFixture.updateFrom(publisher)
        for
          _       <- SweetMockitoLayer[EventPublisherRepository]
                       .whenF2(_.update(publisher.key, update))
                       .thenFail(expectedCause)
          manager <- ZIO.service[EventPublisherManager]
          exit    <- manager.update(publisher.key, update).exit
        yield assert(exit)(
          Assertion.fails(Assertion.hasThrowableCause(Assertion.equalTo(expectedCause)))
        )
      }
    ).provideLayer(
      RepositoryMockLayer ++ ValidatorMockLayer ++ ((RepositoryMockLayer ++ ValidatorMockLayer) >>> ManagerLayer)
    )

  private val RepositoryMockLayer =
    ZLayer.succeed(Mockito.mock(classOf[EventPublisherRepository]))

  private val ValidatorMockLayer =
    ZLayer.succeed(Mockito.mock(classOf[EventPublisherManager.Validator]))

  private val ManagerLayer = ZLayer {
    for
      repository <- ZIO.service[EventPublisherRepository]
      validator  <- ZIO.service[EventPublisherManager.Validator]
    yield EventPublisherManager(repository, validator)
  }
