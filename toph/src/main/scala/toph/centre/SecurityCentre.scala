package toph.centre

import com.softwaremill.macwire.wire
import toph.TophException
import toph.centre.SecurityCentre.Context
import toph.context.TophExecutionContext
import toph.security.AuthorisedAccess
import toph.security.MultiOpenIdProvider
import toph.service.AccountService
import toph.service.TokenService
import zio.IO

trait SecurityCentre:

  def authoriseOpenId(token: String, provider: String, context: Context)(using
      TophExecutionContext,
  ): IO[TophException, Option[AuthorisedAccess]]

object SecurityCentre:

  case class Context(device: String, origin: Option[String])

  def apply(
      multiOpenIdProvider: MultiOpenIdProvider,
      accountService: AccountService,
      tokenService: TokenService,
  ): SecurityCentre =
    wire[Impl]

  class Impl(multiOpenIdProvider: MultiOpenIdProvider, accountService: AccountService, tokenService: TokenService)
      extends SecurityCentre:

    override def authoriseOpenId(token: String, provider: String, context: Context)(using
        TophExecutionContext,
    ): IO[TophException, Option[AuthorisedAccess]] = {
      for {
        (email, protoAccount) <- multiOpenIdProvider
                                   .validate(token, provider)
                                   .mapError(TophException.Security("Unable to validate the oidc token!", _))
                                   .some
        account               <- accountService
                                   .findOrCreate(email, protoAccount)
                                   .mapError(e => Some(TophException.Security("Unable to find or create the account!", e)))
        authorisedAccess      <- tokenService
                                   .authorise(account, context.device, context.origin)
                                   .mapError(e => Some(TophException.Security("Unable to authorise the account!", e)))
      } yield authorisedAccess
    }.unsome
