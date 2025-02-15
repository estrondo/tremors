package toph.module

import com.softwaremill.macwire.wire
import java.time.Period
import javax.crypto.spec.SecretKeySpec
import one.estrondo.moidc4s.OpenIdProvider
import one.estrondo.moidc4s.Provider
import one.estrondo.moidc4s.zio.given
import one.estrondo.moidc4s.zio.http.given
import one.estrondo.moidc4s.zio.json.given
import toph.TimeService
import toph.centre.SecurityCentre
import toph.config.SecurityConfig
import toph.security.MultiOpenIdProvider
import toph.security.TokenCodec
import toph.service.TokenService
import tremors.generator.KeyGenerator
import zio.Scope
import zio.Task
import zio.TaskLayer
import zio.ZIO
import zio.http.Client

class SecurityModule(
    val securityCentre: SecurityCentre,
    val multiOpenIdProvider: MultiOpenIdProvider,
    val tokenService: TokenService,
    val tokenCodec: TokenCodec,
)

object SecurityModule:

  def apply(
      config: SecurityConfig,
      centreModule: CentreModule,
      repositoryModule: RepositoryModule,
      httpModule: HttpModule,
  ): Task[SecurityModule] =
    for openIdProvider <- createOpenIdProvider(config, httpModule.clientLayer)
    yield
      val tokenCodec     = TokenCodec(SecretKeySpec(config.secret.getBytes, config.algorithm))
      val tokenService   = TokenService(repositoryModule.tokenRepository, tokenCodec, TimeService, KeyGenerator, ???)
      val securityCentre = SecurityCentre(openIdProvider, centreModule.accountService, tokenService)
      new SecurityModule(securityCentre, openIdProvider, tokenService, tokenCodec)

  private def createOpenIdProvider(
      config: SecurityConfig,
      clientLayer: TaskLayer[Client & Scope],
  ): Task[MultiOpenIdProvider] =
    implicit val layer: TaskLayer[Client & Scope] = clientLayer
    for providers <- ZIO.foreach(config.openIdProvider) { config =>
                       config.discoveryEndpoint match
                         case Some(url) =>
                           ZIO.logInfo(s"OpenId Provider [${config.id}] @ [$url]") *> (
                             for provider <- OpenIdProvider[Task](Provider.Discovery(url))
                             yield Some((config.id, provider))
                           )

                         case None =>
                           ZIO.logWarning(s"There is no discovery endpoint for ${config.id}.") *> ZIO.none

                     }
    yield MultiOpenIdProvider(providers.collect { case Some(x) => x })
