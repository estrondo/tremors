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
import testkit.core.SweetMockito

object EventPublisherManagerSpec extends Spec:

  override def spec: zio.test.Spec[TestEnvironment & Scope, Any] =
    suite("EventPublisherManager")(
      test("should insert a valid EventPublisher into repository.") {
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
      test("should report any invalid EventPublisher.") {
        for
          validator             <- ZIO.service[EventPublisherManager.Validator]
          manager               <- ZIO.service[EventPublisherManager]
          repository            <- ZIO.service[EventPublisherRepository]
          expectedPublisher      = EventPublisherFixture.createRandom()
          expectedCause          = EventPublisher.Cause("I don't like this Publisher!")
          expectedThrowableCause = GraboidException.Invalid(expectedCause.reason)
          _                      = SweetMockito.failF(validator(eqTo(expectedPublisher)))(
                                     EventPublisher.Invalid(expectedPublisher, Seq(expectedCause))
                                   )
          exit                  <- manager.add(expectedPublisher).exit
        yield assert(exit)(
          Assertion.fails(
            Assertion.isSubtype[GraboidException.IllegalRequest](Assertion.anything)
          )
        )
      },
      test("should not insert into repository when validator fails.") {
        for
          validator             <- ZIO.service[EventPublisherManager.Validator]
          manager               <- ZIO.service[EventPublisherManager]
          repository            <- ZIO.service[EventPublisherRepository]
          expectedPublisher      = EventPublisherFixture.createRandom()
          expectedCause          = EventPublisher.Cause("I don't like this Publisher!")
          expectedThrowableCause = GraboidException.Invalid(expectedCause.reason)
          _                      = SweetMockito.failF(validator(eqTo(expectedPublisher)))(
                                     EventPublisher.Invalid(expectedPublisher, Seq(expectedCause))
                                   )
          exit                  <- manager.add(expectedPublisher).exit
        yield assertTrue(
          verify(repository, never()).add(expectedPublisher) == null
        )
      },
      test("should report any repository failure.") {
        for
          validator        <- ZIO.service[EventPublisherManager.Validator]
          manager          <- ZIO.service[EventPublisherManager]
          repository       <- ZIO.service[EventPublisherRepository]
          expectedThrowable = IllegalStateException("!!!")
          expectedPublisher = EventPublisherFixture.createRandom()
          _                 = SweetMockito
                                .returnF(validator(eqTo(expectedPublisher)))(expectedPublisher)
          _                 = SweetMockito
                                .failF(repository.add(eqTo(expectedPublisher)))(expectedThrowable)
          exit             <- manager.add(expectedPublisher).exit
        yield assert(exit)(
          Assertion.fails(Assertion.hasThrowableCause(Assertion.equalTo(expectedThrowable)))
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
