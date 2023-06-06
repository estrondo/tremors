package webapi.module

import io.grpc.Metadata
import io.grpc.Status
import io.grpc.StatusException
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import one.estrondo.jotawt.Jotawt
import one.estrondo.jotawt.JWKSEndpoint
import one.estrondo.jotawt.JWKSFetcher
import one.estrondo.jotawt.KeyProvider
import one.estrondo.jotawt.KeyProviderRunner
import one.estrondo.jotawt.KeyReader
import one.estrondo.jotawt.KeyRegistry
import one.estrondo.jotawt.KeyRegistrySubscriber
import one.estrondo.jotawt.OpenIDConfigurationEndpoint
import one.estrondo.jotawt.URLHttpClient
import one.estrondo.jotawt.scala_jwt_zio.JwtZIOFramework
import one.estrondo.jotawt.zio.given
import one.estrondo.jotawt.zio_json.ZIOJsonReader
import pdi.jwt.JwtClaim
import scala.concurrent.duration.FiniteDuration
import scalapb.zio_grpc.RequestContext
import webapi.WebAPIException
import webapi.config.OpenIDConfig
import webapi.config.OpenIDProviderConfig
import webapi.model.UserClaims
import zio.IO
import zio.Task
import zio.Unsafe
import zio.ZIO
import zio.json.ast.Json
import zio.json.ast.Json.Obj
import zio.json.ast.Json.Str

trait OpenIDModule:

  def getUserClaims(request: RequestContext): IO[StatusException, UserClaims]

object OpenIDModule:

  def apply(config: OpenIDConfig): Task[OpenIDModule] =
    for
      runtime  <- ZIO.runtime
      registry <- ZIO.attempt(KeyRegistry(subscriber = Some(Subscriber(runtime))))
      executor  = Executors.newScheduledThreadPool(1)
      fetcher   = JWKSFetcher(URLHttpClient, ZIOJsonReader)
      runners  <- ZIO.foreach(config.providers)(createRunner)
      _        <- registry.use(executor, fetcher, KeyReader, runners.values.toSeq*)
      jotawt    = Jotawt(JwtZIOFramework, registry, ZIOJsonReader)
    yield Impl(jotawt)

  private def createRunner(name: String, config: OpenIDProviderConfig): Task[(String, KeyProviderRunner)] =
    ZIO.attempt {
      name -> KeyProviderRunner(
        KeyProvider(
          name = name,
          delay = FiniteDuration(config.jwksTTL.toMillis(), TimeUnit.MILLISECONDS),
          source = (config.jwksEndpoint, config.discoveryEndpoint) match
            case (Some(endpoint), _) => JWKSEndpoint(endpoint)
            case (_, Some(endpoint)) => OpenIDConfigurationEndpoint(endpoint)
            case _                   =>
              throw WebAPIException.InvalidConfig("There is no configuration for jwksEndpoint or discoveryEndpoint!")
        )
      )
    }

  private class Impl(jotawt: Jotawt[JwtClaim, Json, Task]) extends OpenIDModule:

    private val AuthorizationHeader = Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER)

    private val BearerRegex = "^Bearer (.+)$".r

    override def getUserClaims(request: RequestContext): IO[StatusException, UserClaims] =
      (
        for
          header <-
            request.metadata
              .get(AuthorizationHeader)
              .someOrFail(StatusException(Status.UNAUTHENTICATED.withDescription("There is no Authorization header!")))

          claims <- header match
                      case BearerRegex(token) =>
                        validateToken(token)
                      case _                  =>
                        ZIO.fail(Status.UNAUTHENTICATED.withDescription("Invalid Authorization header!").asException())
        yield claims
      ).tapErrorCause(ZIO.logWarningCause("Authorization has failed!", _))

    private def validateToken(token: String): IO[StatusException, UserClaims] =
      for claims <- jotawt
                      .decodeJson(token)
                      .mapError(Status.UNAUTHENTICATED.withCause(_).asException())
      yield
        val obj   = claims.asInstanceOf[Obj]
        val sub   = for case Str(sub) <- obj.get("sub") yield sub
        val email = for case Str(email) <- obj.get("email") yield email

        UserClaims(sub = sub, email = email)

  private class Subscriber(runtime: zio.Runtime[Any])(using Unsafe) extends KeyRegistrySubscriber:

    override def insertedKid(provider: KeyProvider, kid: String): Unit =
      run {
        ZIO.logInfo(s"A new kid=${kid} from ${provider.name} has been inserted.")
      }

    override def removedKid(provider: KeyProvider, kid: String): Unit =
      run {
        ZIO.logInfo(s"A kid=${kid} from ${provider.name} has been removed.")
      }

    override def scheduled(runner: KeyProviderRunner): Unit =
      run {
        ZIO.logInfo("A runner has been added.")
      }

    override def updatedKid(provider: KeyProvider, kid: String): Unit =
      run {
        ZIO.logInfo(s"A kid=$kid from ${provider.name} has been updated.")
      }

    private def run[A](zio: ZIO[Any, Throwable, A]): A =
      runtime.unsafe.run(zio).getOrThrow()
