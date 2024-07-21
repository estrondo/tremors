package toph.security

import java.time.Clock
import java.time.LocalDate
import java.time.LocalTime
import java.time.Period
import java.time.ZonedDateTime
import javax.crypto.spec.SecretKeySpec
import one.estrondo.sweetmockito.zio.SweetMockitoLayer
import org.mockito.Mockito
import toph.TimeService
import toph.TophSpec
import toph.model.Account
import toph.model.AccountFixture
import zio.ZIO
import zio.ZLayer
import zio.test.assertTrue

object TokenServiceSpec extends TophSpec:

  private val account = Account(
    key = "einstein-key",
    email = "einstein@mc.2",
    name = "Albert Einstein",
  )

  private val expectedToken =
    "AAAAAHLKAsA=.AAxlaW5zdGVpbi1rZXkAD0FsYmVydCBFaW5zdGVpbgANZWluc3RlaW5AbWMuMg==.Q+7TNAEkK+BQ/nsj8RcePsIjQ/wXFMe5GHNnDAkEOcOCWufV5UlBqyXdX2MPLzFJ0NWBKlFPG9JX16nFGAjBtw=="

  private val now =
    val localDate = LocalDate.of(2030, 12, 31)
    val localTime = LocalTime.of(20, 0, 0)
    ZonedDateTime.of(localDate, localTime, Clock.systemUTC().getZone)

  override def spec = suite("TokenServiceSpec")(
    test("It should encode a token.") {
      for
        _     <- ZIO.serviceWith[TimeService] { service =>
                   Mockito.when(service.zonedDateTimeNow()).thenReturn(now)
                 }
        token <- ZIO.serviceWithZIO[TokenService](_.encode(account))
      yield assertTrue(
        token.token == expectedToken,
      )
    },
    test("It should decode a token.") {
      for
        _      <- ZIO.serviceWith[TimeService] { service =>
                    Mockito.when(service.zonedDateTimeNow()).thenReturn(now.plusSeconds(20))
                  }
        result <-
          ZIO.serviceWithZIO[TokenService](
            _.decode(expectedToken),
          )
      yield assertTrue(
        result.contains(Token(account, expectedToken)),
      )
    },
    test("It should reject a token.") {
      for
        _      <- ZIO.serviceWith[TimeService] { service =>
                    Mockito.when(service.zonedDateTimeNow()).thenReturn(now.plusSeconds(20))
                  }
        result <-
          ZIO.serviceWithZIO[TokenService](
            _.decode("AAAAAHLKAsA=.AAMxMjMAD0FsYmVydCBFaW5zdGVpbgAGZUBtYy4y.ZRtn/gg=="),
          )
      yield assertTrue(result.isEmpty)
    },
  ).provideSome(
    SweetMockitoLayer.newMockLayer[TimeService],
    ZLayer {
      for zonedDateTimeService <- ZIO.service[TimeService]
      yield
        val secretKey =
          SecretKeySpec("A password was defined to be used here, but we can change it.".getBytes, "HmacSHA512")
        TokenService(secretKey, zonedDateTimeService, Period.ofDays(10), B64)
    },
  )
