package toph.security

import one.estrondo.moidc4s.OpenIdProvider
import one.estrondo.moidc4s.jwt.Jwt
import one.estrondo.moidc4s.jwt.zio.given
import one.estrondo.moidc4s.zio.given
import scala.collection.immutable.HashMap
import toph.model.ProtoAccount
import zio.Task
import zio.ZIO
import zio.ZIOAspect
import zio.json.DeriveJsonDecoder
import zio.json.JsonDecoder
import zio.json.ast.Json.*

trait MultiOpenIdProvider:

  /** If everything went well, it returns the user's email. */
  def validate(token: String, provider: String): Task[Option[(String, ProtoAccount)]]

object MultiOpenIdProvider:

  case class Claims(email: Option[String], name: Option[String])
  given JsonDecoder[Claims] = DeriveJsonDecoder.gen[Claims]

  def apply(providers: Iterable[(String, OpenIdProvider[Task])]): MultiOpenIdProvider =
    Impl(HashMap.from(providers))

  private class Impl(map: Map[String, OpenIdProvider[Task]]) extends MultiOpenIdProvider:

    private val evaluators = HashMap.from {
      for ((k, provider) <- map) yield k -> provider.evaluate(Jwt.decode())
    }

    override def validate(token: String, provider: String): Task[Option[(String, ProtoAccount)]] =
      (evaluators.get(provider) match
        case Some(evaluator) =>
          evaluator(token)
            .flatMap {
              _.as[Claims] match
                case Right(Claims(Some(email), name)) =>
                  ZIO.logDebug(s"$email' was identified.") *> ZIO.succeed(Some((email, ProtoAccount(name))))
                case Right(_)                         =>
                  ZIO.logWarning(s"Token with no e-mai.") *> ZIO.none
                case Left(message)                    =>
                  ZIO.logWarning(s"Invalid token: $message") *> ZIO.none
            }

        case None =>
          ZIO.logWarning("No provider.") *> ZIO.none
      ) @@ ZIOAspect.annotated(
        "MultiOpenIdProvider.token"    -> token,
        "MultiOpenIdProvider.provider" -> provider,
      )
