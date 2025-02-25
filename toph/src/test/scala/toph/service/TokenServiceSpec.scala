package toph.service

import java.time.Period
import java.time.temporal.ChronoUnit
import one.estrondo.sweetmockito.Answer
import one.estrondo.sweetmockito.zio.SweetMockitoLayer
import one.estrondo.sweetmockito.zio.given
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.mockito.Mockito.verify
import toph.TimeService
import toph.TophSpec
import toph.centre.SecurityCentreFixture
import toph.model.AccountFixture
import toph.model.Token
import toph.repository.TokenRepository
import toph.security.AuthorisedAccess
import toph.security.AuthorisedAccessFixture
import toph.security.TokenCodec
import tremors.generator.KeyGenerator
import zio.ZIO
import zio.ZLayer
import zio.test.assertTrue

object TokenServiceSpec extends TophSpec:

  private val refreshTokenPeriod = Period.ofMonths(6)
  private val accessTokenPeriod  = Period.ofMonths(6)

  private val now = TimeService.zonedDateTimeNow().truncatedTo(ChronoUnit.MINUTES)

  private val setNow = ZIO.serviceWith[TimeService](x => Mockito.when(x.zonedDateTimeNow()).thenReturn(now))

  override def spec = suite("TokenService")(
    test("It should authorise an account and store.") {

      val expectedAccount          = AccountFixture.createRandom()
      val securityContext          = SecurityCentreFixture.createRandomContext()
      val expectedAuthorisedAccess = AuthorisedAccessFixture
        .createRandom()
        .copy(
          account = expectedAccount,
        )

      val expectedAccessTokenExpiration = now.plus(accessTokenPeriod).truncatedTo(ChronoUnit.MINUTES)

      for
        _                <- setNow
        _                <- SweetMockitoLayer[TokenCodec]
                              .whenF2(_.encode(expectedAccount, expectedAccessTokenExpiration))
                              .thenReturn(expectedAuthorisedAccess.accessToken)
        _                <- ZIO.serviceWith[KeyGenerator] { x =>
                              Mockito.when(x.medium()).thenReturn(expectedAuthorisedAccess.refreshToken)
                            }
        _                <- SweetMockitoLayer[TokenRepository]
                              .whenF2(_.add(ArgumentMatchers.any()))
                              .thenAnswer(inv => Answer.succeed(inv.getArgument[Token](0)))
        authorisedAccess <- ZIO.serviceWithZIO[TokenService](
                              _.authorise(
                                account = expectedAccount,
                                device = securityContext.device,
                                origin = securityContext.origin,
                              ),
                            )
        repository       <- ZIO.service[TokenRepository]
      yield assertTrue(
        authorisedAccess == expectedAuthorisedAccess,
        verify(repository).add(ArgumentMatchers.any()) == null,
      )
    },
  ).provideSome(
    ZLayer.succeed(TokenService.Config(accessTokenPeriod, refreshTokenPeriod)),
    SweetMockitoLayer.newMockLayer[TokenRepository],
    SweetMockitoLayer.newMockLayer[TokenCodec],
    SweetMockitoLayer.newMockLayer[TimeService],
    SweetMockitoLayer.newMockLayer[KeyGenerator],
    ZLayer.fromFunction(TokenService.apply),
  )
