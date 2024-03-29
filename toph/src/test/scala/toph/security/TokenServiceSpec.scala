package toph.security

import java.time.Clock
import java.time.LocalDate
import java.time.LocalTime
import java.time.Period
import java.time.ZonedDateTime
import javax.crypto.spec.SecretKeySpec
import one.estrondo.sweetmockito.zio.SweetMockitoLayer
import org.mockito.Mockito
import toph.TophSpec
import toph.ZonedDateTimeService
import zio.ZIO
import zio.ZLayer
import zio.test.assertTrue

object TokenServiceSpec extends TophSpec:

  val claims: Claims = Claims(
    id = "123",
    name = "Albert Einstein",
    email = "e@mc.2"
  )

  val expectedToken =
    "AAAAAHLKAsA=.AAMxMjMAD0FsYmVydCBFaW5zdGVpbgAGZUBtYy4y.9SxDgApXFYiSS/PMunoJoSXDQufdxrH7ath86VDWSLhU/fmy38CPL+zPA/NZu9be2O72MloRot+r0b+w+w0kdA=="

  val now: ZonedDateTime =
    val localDate = LocalDate.of(2030, 12, 31)
    val localTime = LocalTime.of(20, 0, 0)
    ZonedDateTime.of(localDate, localTime, Clock.systemUTC().getZone)

  override def spec = suite("TokenServiceSpec")(
    test("It should encode a token.") {
      for
        _     <- ZIO.serviceWith[ZonedDateTimeService] { service =>
                   Mockito.when(service.now()).thenReturn(now)
                 }
        token <- ZIO.serviceWithZIO[TokenService](_.encode(claims))
      yield assertTrue(
        token == expectedToken
      )
    },
    test("It should decode a token.") {
      for
        _      <- ZIO.serviceWith[ZonedDateTimeService] { service =>
                    Mockito.when(service.now()).thenReturn(now.plusSeconds(20))
                  }
        result <-
          ZIO.serviceWithZIO[TokenService](
            _.decode(expectedToken)
          )
      yield assertTrue(
        result.contains(claims)
      )
    },
    test("It should reject a token.") {
      for
        _      <- ZIO.serviceWith[ZonedDateTimeService] { service =>
                    Mockito.when(service.now()).thenReturn(now.plusSeconds(20))
                  }
        result <-
          ZIO.serviceWithZIO[TokenService](
            _.decode("AAAAAHLKAsA=.AAMxMjMAD0FsYmVydCBFaW5zdGVpbgAGZUBtYy4y.ZRtn/gg==")
          )
      yield assertTrue(result.isEmpty)
    }
  ).provideSome(
    SweetMockitoLayer.newMockLayer[ZonedDateTimeService],
    ZLayer {
      for zonedDateTimeService <- ZIO.service[ZonedDateTimeService]
      yield
        val secretKey = SecretKeySpec("A password was defined to be used here, but we can change it.".getBytes, "HmacSHA512")
        TokenService(secretKey, zonedDateTimeService, Period.ofDays(10))
    }
  )
