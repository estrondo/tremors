package tremors.zio.starter

import com.typesafe.config.Config as TypesafeConfig
import com.typesafe.config.ConfigFactory
import com.typesafe.config.ConfigParseOptions
import com.typesafe.config.ConfigResolveOptions
import com.typesafe.config.ConfigSyntax
import zio.ConfigProvider
import zio.Runtime
import zio.Scope
import zio.System
import zio.Task
import zio.ULayer
import zio.ZIO
import zio.ZLayer
import zio.config.magnolia.DeriveConfig
import zio.config.typesafe.*
import zio.logging.backend.SLF4J

object ZioStarter:

  final case class Profile(value: String)

  val logging: ULayer[Scope] =
    Runtime.removeDefaultLoggers >>> SLF4J.slf4j >>> Scope.default

  def apply[C: DeriveConfig](): Task[(C, Option[Profile])] =
    val parseOptions = ConfigParseOptions
      .defaults()
      .setSyntax(ConfigSyntax.CONF)
      .setAllowMissing(false)

    val resolveOptions = ConfigResolveOptions
      .defaults()
      .setUseSystemEnvironment(true)
      .setAllowUnresolved(false)

    for
      defaultConfig <- ZIO.attemptBlocking(ConfigFactory.defaultApplication(parseOptions))
      configuration <- System
                         .env("TREMORS_PROFILE")
                         .orElse(System.property("tremors.profile"))
                         .flatMap(loadProfile(defaultConfig, parseOptions, resolveOptions))
    yield configuration

  private def loadProfile[C: DeriveConfig](
      defaultConfig: TypesafeConfig,
      parseOptions: ConfigParseOptions,
      resolveOptions: ConfigResolveOptions,
  )(
      option: Option[String],
  ): Task[(C, Option[Profile])] =
    for
      tuple <- ZIO.attemptBlocking {
                 option match
                   case Some(profileName) =>
                     ConfigFactory
                       .parseResources(s"application-${profileName}.conf", parseOptions)
                       .withFallback(defaultConfig) -> Some(Profile(profileName))

                   case None =>
                     (defaultConfig, None)
               }

      result <- ConfigProvider.fromTypesafeConfig(tuple._1.resolve(resolveOptions)).load(summon[DeriveConfig[C]].desc)
    yield (result, tuple._2)
