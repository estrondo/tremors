package tremors.graboid

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import com.typesafe.config.ConfigParseOptions
import com.typesafe.config.ConfigSyntax
import tremors.graboid.config.*
import zio.UIO
import zio._
import zio.config.ConfigSource
import zio.config.magnolia.Descriptor
import zio.config.magnolia.Descriptor.derived
import zio.config.magnolia.descriptor
import zio.config.read
import zio.config.toKebabCase
import zio.config.typesafe.TypesafeConfigSource
import scala.util.Try

object ConfigModule:

  def apply(): ConfigModule = ConfigModuleImpl()

trait ConfigModule:

  def config: Task[GraboidConfig]

private class ConfigModuleImpl extends ConfigModule:

  case class C(graboid: GraboidConfig)

  def parseOptions = ConfigParseOptions
    .defaults()
    .setSyntax(ConfigSyntax.CONF)
    .setAllowMissing(false)

  def config: Task[GraboidConfig] =
    for
      _               <- ZIO.logInfo("Loading application configuration.")
      propertyProfile <- System.property("tremors.profile")
      envProfile      <- System.env("TREMORS_PROFILE")
      configuration   <- propertyProfile.orElse(envProfile) match
                           case Some(profile) => loadProfile(profile.toLowerCase())
                           case None          =>
                             ZIO.logInfo("Loading default configuration.") &> ZIO.attemptBlockingIO(
                               defaultApplicationConfig
                             )
      config          <- read(
                           descriptor[C].mapKey(toKebabCase) from TypesafeConfigSource
                             .fromTypesafeConfig(
                               ZIO.attempt(configuration.resolve())
                             )
                         )
    yield config.graboid

  private def defaultApplicationConfig = ConfigFactory.defaultApplication(parseOptions)

  private def loadProfile(profile: String): Task[Config] =
    for
      _      <- ZIO.logInfo(s"Loading profile [$profile].")
      config <- ZIO.attemptBlocking {
                  try
                    ConfigFactory
                      .parseResources(s"application-$profile.conf", parseOptions)
                      .withFallback(defaultApplicationConfig)
                  catch
                    case cause: Exception =>
                      cause.printStackTrace()
                      throw GraboidException.Unexpected(s"Impossible to load $profile!", cause)
                }
    yield config

  private def toArangoHost(value: String): Seq[ArangoHost] =
    for part <- value.split("\\s*,\\s*")
    yield part.split(":") match
      case Array(hostname, port) => ArangoHost(hostname, port.toInt)
      case _                     => throw IllegalArgumentException(value)

  private def fromArangoHost(values: Seq[ArangoHost]): String = throw IllegalStateException(
    "fromArangoHost"
  )

  private given Descriptor[Seq[ArangoHost]] =
    Descriptor.from(Descriptor[String].transform(toArangoHost, fromArangoHost))

  private given Descriptor[ArangoConfig] = derived[ArangoConfig]
