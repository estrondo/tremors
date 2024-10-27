package toph.module

import java.time.Period
import javax.crypto.spec.SecretKeySpec
import one.estrondo.moidc4s.OpenIdProvider
import one.estrondo.moidc4s.Provider
import one.estrondo.moidc4s.zio.given
import one.estrondo.moidc4s.zio.http.given
import one.estrondo.moidc4s.zio.json.given
import scala.collection.immutable.HashMap
import toph.TimeService
import toph.centre.SecurityCentre
import toph.config.SecurityConfig
import toph.security.B64
import toph.security.MultiOpenIdProvider
import toph.security.TokenService
import zio.Scope
import zio.Task
import zio.TaskLayer
import zio.ZIO
import zio.http.Client

class SecurityModule(
    val securityCentre: SecurityCentre,
    val multiOpenIdProvider: MultiOpenIdProvider,
    val tokenService: TokenService,
)

object SecurityModule:

  def apply(config: SecurityConfig, centreModule: CentreModule, httpModule: HttpModule): Task[SecurityModule] =
    for openIdProvider <- createOpenIdProvider(config, httpModule.clientLayer)
    yield
      val tokenService = TokenService(
        SecretKeySpec(config.secret.getBytes, config.algorithm),
        TimeService,
        Period.ofDays(config.tokenExpiration),
        B64,
      )

      val securityCentre = SecurityCentre(openIdProvider, centreModule.accountService, tokenService)
      new SecurityModule(securityCentre, openIdProvider, tokenService)

  private def createOpenIdProvider(
      config: SecurityConfig,
      clientLayer: TaskLayer[Client & Scope],
  ): Task[MultiOpenIdProvider] =
    implicit val layer: TaskLayer[Client & Scope] = clientLayer
    for providers <- ZIO.foreach(config.openIdProvider) { config =>
                       config.discoveryEndpoint match {
                         case Some(url) =>
                           ZIO.logInfo(s"OpenId Provider [${config.id}] @ [$url]") *> (
                             for provider <- OpenIdProvider[Task](Provider.Discovery(url))
                             yield Some((config.id, provider))
                           )

                         case None =>
                           ZIO.logWarning(s"There is no discovery endpoint for ${config.id}.") *> ZIO.none
                       }
                     }
    yield MultiOpenIdProvider(providers.collect { case Some(x) => x })
