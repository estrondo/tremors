package tremors.graboid

import zio.UIO
import zio.config.ConfigSource
import zio.config.typesafe.TypesafeConfigSource
import zio._
import com.typesafe.config.ConfigFactory
import com.typesafe.config.Config
import com.typesafe.config.ConfigParseOptions
import com.typesafe.config.ConfigSyntax

object ConfigModule:

  def apply(): ConfigModule = ConfigModuleImpl()

trait ConfigModule:

  def configSource: Task[ConfigSource]

private class ConfigModuleImpl extends ConfigModule:

  def parseOptions = ConfigParseOptions
    .defaults()
    .setSyntax(ConfigSyntax.CONF)
    .setAllowMissing(false)

  def configSource: Task[ConfigSource] =
    for
      _               <- ZIO.logInfo("Loading application configuration.")
      propertyProfile <- System.property("tremors.profile")
      envProfile      <- System.env("TREMORS_PROFILE")
      configuration   <- propertyProfile.orElse(envProfile) match
                           case Some(profile) => loadProfile(profile.toLowerCase())
                           case None          => ZIO.attemptBlockingIO(defaultApplicationConfig)
    yield TypesafeConfigSource.fromTypesafeConfig(ZIO.succeed(configuration))

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
