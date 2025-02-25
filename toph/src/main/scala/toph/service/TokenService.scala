package toph.service

import com.softwaremill.macwire.wire
import java.security.MessageDigest
import java.time.Period
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import toph.TimeService
import toph.TophException
import toph.model.Account
import toph.model.Token
import toph.repository.TokenRepository
import toph.security.AccessToken
import toph.security.AuthorisedAccess
import toph.security.TokenCodec
import tremors.generator.KeyGenerator
import zio.Task
import zio.UIO
import zio.ZIO

trait TokenService:

  def authorise(account: Account, device: String, origin: Option[String]): Task[AuthorisedAccess]

  def verify(accessToken: Array[Byte]): Task[AccessToken]

object TokenService:

  case class Config(accessTokenExpiration: Period, refreshTokenExpiration: Period)

  def apply(
      repository: TokenRepository,
      tokenCodec: TokenCodec,
      timeService: TimeService,
      keyGenerator: KeyGenerator,
      config: Config,
  ): TokenService =
    wire[Impl]

  class Impl(
      repository: TokenRepository,
      tokenCodec: TokenCodec,
      timeService: TimeService,
      keyGenerator: KeyGenerator,
      config: Config,
  ) extends TokenService:

    private val getNow = ZIO.succeed {
      timeService.zonedDateTimeNow().truncatedTo(ChronoUnit.SECONDS)
    }

    override def authorise(account: Account, device: String, origin: Option[String]): Task[AuthorisedAccess] =
      for
        (now, accessTokenExp, refreshTokenExp) <- calculateTime()
        accessToken                            <- tokenCodec
                                                    .encode(account, accessTokenExp)
                                                    .mapError(TophException.Security("Unable to encode the access token!", _))
        (authorisedAccess, token)              <- createAccess(
                                                    account = account,
                                                    accessToken = accessToken,
                                                    now = now,
                                                    accessExp = accessTokenExp,
                                                    refreshExp = refreshTokenExp,
                                                    device = device,
                                                    origin = origin,
                                                  )
                                                    .mapError(TophException.Security("Unable to create tokens!", _))
        _                                      <- repository
                                                    .add(token)
                                                    .mapError(TophException.Security("Unable to store token!", _))
      yield authorisedAccess

    override def verify(accessToken: Array[Byte]): Task[AccessToken] =
      for
        now     <- getNow
        account <- tokenCodec.decode(accessToken, now)
      yield AccessToken(account, accessToken)

    private def calculateTime(): UIO[(ZonedDateTime, ZonedDateTime, ZonedDateTime)] = ZIO.succeed {
      val now                    = timeService.zonedDateTimeNow().truncatedTo(ChronoUnit.SECONDS)
      val accessTokenExpiration  = now.plus(config.accessTokenExpiration).truncatedTo(ChronoUnit.SECONDS)
      val refreshTokenExpiration = accessTokenExpiration
        .plus(config.refreshTokenExpiration)
        .truncatedTo(ChronoUnit.SECONDS)

      (now, accessTokenExpiration, refreshTokenExpiration)
    }

    private def createAccess(
        account: Account,
        accessToken: Array[Byte],
        now: ZonedDateTime,
        accessExp: ZonedDateTime,
        refreshExp: ZonedDateTime,
        device: String,
        origin: Option[String],
    ): Task[(AuthorisedAccess, Token)] =
      ZIO.attempt {
        val md           = MessageDigest.getInstance("MD5")
        val hash         = BigInt(1, md.digest(accessToken)).toString(32)
        val refreshToken = keyGenerator.medium()

        val authorisedAccess = AuthorisedAccess(
          account = account,
          accessToken = accessToken,
          refreshToken = refreshToken,
        )

        val token = Token(
          key = refreshToken,
          expiration = refreshExp,
          accountKey = account.key,
          accountEmail = account.email,
          accessTokenHash = hash,
          accessTokenExpiration = accessExp,
          device = device,
          origin = origin,
          createdAt = now,
        )

        (authorisedAccess, token)
      }
