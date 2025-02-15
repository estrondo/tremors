package toph.security

import java.time.Clock
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZonedDateTime
import javax.crypto.spec.SecretKeySpec
import toph.TophSpec
import toph.model.Account
import zio.ZIO
import zio.ZLayer
import zio.test.assertTrue
import zio.test.given

object TokenCodecSpec extends TophSpec:

  private val account = Account(
    key = "einstein-key",
    email = "einstein@mc.2",
    name = "Albert Einstein",
  )

  private val expectedToken =
    Array[Byte](1, 0, 0, 0, 0, 114, -68, -45, -64, 0, 63, 0, 0, 0, 0, 0, 12, 101, 105, 110, 115, 116, 101, 105, 110, 45,
      107, 101, 121, 0, 13, 101, 105, 110, 115, 116, 101, 105, 110, 64, 109, 99, 46, 50, 0, 15, 65, 108, 98, 101, 114,
      116, 32, 69, 105, 110, 115, 116, 101, 105, 110, 0, 0, -8, -2, -7, -59, 53, 44, 62, 82, 62, 109, 50, 79, -71, -44,
      -70, 47, -79, -122, 17, -102, 17, 9, 90, -60, -46, -1, 43, 95, -112, 101, -74, 8, 39, -104, 73, -43, 99, -58,
      -110, -123, -64, 58, -86, 25, 111, -61, 77, -51, -19, -38, 29, 79, 80, 53, -53, -85, 12, 118, 22, 18, 69, -125,
      -95, -20)

  private val expiration =
    val localDate = LocalDate.of(2030, 12, 31)
    val localTime = LocalTime.of(20, 0, 0)
    ZonedDateTime.of(localDate, localTime, Clock.systemUTC().getZone)

  override def spec = suite("TokenCodeSpec")(
    test("It should encode a token.") {
      for token <- ZIO.serviceWithZIO[TokenCodec](_.encode(account, expiration))
      yield assertTrue(
        token sameElements expectedToken,
      )
    },
    test("It should decode a token.") {
      for result <- ZIO.serviceWithZIO[TokenCodec](
                      _.decode(expectedToken, expiration.plusSeconds(10)),
                    )
      yield assertTrue(
        result == account,
      )
    },
    test("It should reject a token.") {
      for result <-
          ZIO
            .serviceWithZIO[TokenCodec](
              _.decode(
                Array[Byte](0x01) ++ "AAAAAHLKAsA=.AAMxMjMAD0FsYmVydCBFaW5zdGVpbgAGZUBtYy4y.ZRtn/gg==".getBytes,
                expiration,
              ),
            )
            .exit
      yield assertTrue(result.is(_.failure).getMessage == "Expired token!")
    },
  ).provideSome(
    ZLayer.succeed {
      val secretKey =
        SecretKeySpec("A password was defined to be used here, but we can change it.".getBytes, "HmacSHA512")
      TokenCodec(secretKey)
    },
  )
