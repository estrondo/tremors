package toph.security

import java.time.Clock
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZonedDateTime
import java.util.random.RandomGenerator
import javax.crypto.spec.SecretKeySpec
import one.estrondo.sweetmockito.zio.SweetMockitoLayer
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
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
    Array[Byte](1, 0, 0, 0, 0, 114, -68, -45, -64, 0, 63, 0, 0, 0, 1, 0, 12, 101, 105, 110, 115, 116, 101, 105, 110, 45,
      107, 101, 121, 0, 13, 101, 105, 110, 115, 116, 101, 105, 110, 64, 109, 99, 46, 50, 0, 15, 65, 108, 98, 101, 114,
      116, 32, 69, 105, 110, 115, 116, 101, 105, 110, 0, 0, -107, 87, -56, 6, -98, 30, 77, -39, -1, -22, 7, -85, 107,
      -19, 61, 2, 23, 58, -11, -9, 5, 24, -70, -123, 9, 28, 20, 78, -105, -6, -10, 111, 54, 124, -103, 103, -27, -79,
      -102, -39, 120, -20, -46, 22, 76, -97, 24, 37, -31, -38, 2, -65, 9, -85, -107, -45, 118, 9, -12, 87, -4, -57, -32,
      -106)

  private val expiration =
    val localDate = LocalDate.of(2030, 12, 31)
    val localTime = LocalTime.of(20, 0, 0)
    ZonedDateTime.of(localDate, localTime, Clock.systemUTC().getZone)

  private val generate1 =
    ZIO.serviceWith[RandomGenerator] { rg =>
      Mockito.when(rg.nextInt(any())).thenReturn(1)
    }

  override def spec = suite("TokenCodeSpec")(
    test("It should encode a token.") {
      for
        _     <- generate1
        token <- ZIO.serviceWithZIO[TokenCodec](_.encode(account, expiration))
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
    SweetMockitoLayer.newMockLayer[RandomGenerator],
    ZLayer(
      ZIO.serviceWith[RandomGenerator] { randomGenerator =>
        val keys = IndexedSeq(
          SecretKeySpec("It is supposed to user other secret, because this one is obsolete!.".getBytes, "HmacSHA512"),
          SecretKeySpec("A password was defined to be used here, but we can change it.".getBytes, "HmacSHA512"),
        )
        TokenCodec(keys, randomGenerator)
      },
    ),
  )
