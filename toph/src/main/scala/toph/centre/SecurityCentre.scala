package toph.centre

import toph.TophException
import toph.extractSomeError
import toph.security.MultiOpenIdProvider
import toph.security.Token
import toph.security.TokenService
import toph.service.AccountService
import zio.IO

trait SecurityCentre:

  def authorise(token: String, provider: String): IO[TophException, Option[Token]]

object SecurityCentre:

  def apply(
      multiOpenIdProvider: MultiOpenIdProvider,
      accountService: AccountService,
      tokenService: TokenService,
  ): SecurityCentre = Impl(
    multiOpenIdProvider,
    accountService,
    tokenService,
  )

  private class Impl(
      multiOpenIdProvider: MultiOpenIdProvider,
      accountService: AccountService,
      tokenService: TokenService,
  ) extends SecurityCentre:

    override def authorise(token: String, provider: String): IO[TophException, Option[Token]] = {
      for {
        email   <- multiOpenIdProvider
                     .validate(token, provider)
                     .mapError(TophException.Security("Unable to validate!", _))
                     .some
        account <- accountService
                     .findOrCreate(email)
                     .mapError(TophException.Security("Unable to find or create the account.", _))
      } yield {
        tokenService
          .encode(account)
          .mapError(TophException.Security("Unable to create a token!", _))
      }
    }.flatten.extractSomeError
