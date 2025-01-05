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
    Array[Byte](1, 0, 0, 0, 0, 114, -66, 37, 64, 0, 63, 0, 0, 0, 0, 0, 12, 101, 105, 110, 115, 116, 101, 105, 110, 45,
      107, 101, 121, 0, 13, 101, 105, 110, 115, 116, 101, 105, 110, 64, 109, 99, 46, 50, 0, 15, 65, 108, 98, 101, 114,
      116, 32, 69, 105, 110, 115, 116, 101, 105, 110, 0, 0, 8, 108, 30, -27, -39, -113, 28, 124, 37, -1, -27, -16, -116,
      92, 23, 49, 105, -77, 98, 101, 15, 120, 123, 1, 93, 41, 15, 55, -106, -110, -34, -67, -71, -9, -42, 67, -42, -97,
      -90, -60, -119, -122, -119, -59, 51, -2, 16, 88, 102, -49, 110, 34, 2, 102, -9, -72, 23, 30, -122, -98, 30, -127,
      3, 56)

  private val now =
    val localDate = LocalDate.of(2030, 12, 31)
    val localTime = LocalTime.of(20, 0, 0)
    ZonedDateTime.of(localDate, localTime, Clock.systemUTC().getZone)

  private val period = Period.ofDays(1)

  override def spec = suite("TokenServiceSpec")(
    test("It should encode a token.") {
      for
        _     <- ZIO.serviceWith[TimeService] { service =>
                   Mockito.when(service.zonedDateTimeNow()).thenReturn(now)
                 }
        token <- ZIO.serviceWithZIO[TokenService](_.encode(account))
      yield assertTrue(
        token.token sameElements expectedToken,
      )
    },
    test("It should decode a token.") {
      for
        _      <- ZIO.serviceWith[TimeService] { service =>
                    Mockito.when(service.zonedDateTimeNow()).thenReturn(now.plus(period).plusSeconds(10))
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
            _.decode("AAAAAHLKAsA=.AAMxMjMAD0FsYmVydCBFaW5zdGVpbgAGZUBtYy4y.ZRtn/gg==".getBytes),
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
        TokenService(secretKey, zonedDateTimeService, period, B64)
    },
  )
