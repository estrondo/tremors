package toph.module

import java.time.Period
import javax.crypto.spec.SecretKeySpec
import toph.ZonedDateTimeService
import toph.config.SecurityConfig
import toph.grpc.Authenticator
import toph.security.TokenService
import zio.Task
import zio.ZIO

trait SecurityModule:

  val authenticator: Authenticator

object SecurityModule:

  def apply(centreModule: CentreModule, config: SecurityConfig): Task[SecurityModule] =
    for
      tokenService  <- ZIO.attempt {
                         TokenService(
                           SecretKeySpec(config.secret.getBytes, config.algorithm),
                           ZonedDateTimeService,
                           Period.ofDays(config.tokenExpiration)
                         )
                       }
      authenticator <- Authenticator(tokenService)
    yield Impl(authenticator)

  private class Impl(
      val authenticator: Authenticator
  ) extends SecurityModule
