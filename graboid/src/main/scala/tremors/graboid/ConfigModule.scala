package tremors.graboid

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import com.typesafe.config.ConfigParseOptions
import com.typesafe.config.ConfigSyntax
import zio.UIO
import zio._
import zio.config.ConfigSource
import zio.config.magnolia.descriptor
import zio.config.read
import zio.config.typesafe.TypesafeConfigSource

object ConfigModule:

  def apply(): ConfigModule = ConfigModuleImpl()

trait ConfigModule:

  def config: Task[GraboidConfig]

private class ConfigModuleImpl extends ConfigModule:

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
                           case None          => ZIO.attemptBlockingIO(defaultApplicationConfig)
      config          <- read(
                           descriptor[GraboidConfig] from TypesafeConfigSource.fromTypesafeConfig(
                             ZIO.succeed(configuration)
                           )
                         )
    yield config

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
                      throw GraboidException.NotFound(s"Impossible to load $profile!", cause)
                }
    yield config
