package toph.centre

import toph.TophException
import toph.context.TophExecutionContext
import toph.extractSomeError
import toph.security.MultiOpenIdProvider
import toph.security.Token
import toph.security.TokenService
import toph.service.AccountService
import zio.IO

trait SecurityCentre:

  def authoriseOpenId(token: String, provider: String)(using TophExecutionContext): IO[TophException, Option[Token]]

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

    override def authoriseOpenId(token: String, provider: String)(using
        TophExecutionContext,
    ): IO[TophException, Option[Token]] = {
      for {
        (email, protoAccount) <- multiOpenIdProvider
                                   .validate(token, provider)
                                   .mapError(TophException.Security("Unable to validate the oidc token!", _))
                                   .some
        account               <- accountService
                                   .findOrCreate(email, protoAccount)
                                   .mapError(TophException.Security("Unable to find or create the account!", _))
        token                 <- tokenService
                                   .encode(account)
                                   .mapError(TophException.Security("Unable to encode a token!", _))
      } yield token
    }.extractSomeError
