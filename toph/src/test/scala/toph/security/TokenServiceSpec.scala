package toph.security

import java.time.Clock
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
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
    "AAAAAHK808o=.AAMxMjMAD0FsYmVydCBFaW5zdGVpbgAGZUBtYy4y.loRQ+m3E83HZLd1Uiar2NMk1nvdtnBKtGnWEdTFCGLSN543FInmpgSt6DMNDOYj0ZVFAZUuG6Lf7wdBZRtn/gg=="

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
            _.decode("AAAAAHK808o=.AAMxMjMAD0FsYmVydCBFaW5zdGVpbgAGZUBtYy4y.ZRtn/gg==")
          )
      yield assertTrue(result.isEmpty)
    }
  ).provideSome(
    SweetMockitoLayer.newMockLayer[ZonedDateTimeService],
    ZLayer {
      for zonedDateTimeService <- ZIO.service[ZonedDateTimeService]
      yield
        val secretKey = SecretKeySpec("A super long or maybe confused secret key to be used!".getBytes, "HmacSHA512")
        TokenService(secretKey, zonedDateTimeService, Duration.ofSeconds(10))
    }
  )
