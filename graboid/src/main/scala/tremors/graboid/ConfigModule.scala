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
      _             <- ZIO.logInfo("Loading application configuration.")
      application   <- loadTypeSafeApplication()
      envProfie     <- System.env("GRABOID_PROFILE")
      configuration <- envProfie match
                         case Some(profile) => loadProfile(profile.toLowerCase(), application)
                         case None          => ZIO.succeed(application)
    yield TypesafeConfigSource.fromTypesafeConfig(ZIO.succeed(configuration))

  private def loadTypeSafeApplication(): Task[Config] =
    ZIO.attemptBlockingIO {
      ConfigFactory.defaultApplication(parseOptions)
    }

  private def loadProfile(profile: String, application: Config): Task[Config] =
    for
      _      <- ZIO.logDebug(s"Loading profile [$profile].")
      config <- ZIO.attemptBlockingIO {
                  try
                    ConfigFactory
                      .parseResources(s"$profile.conf", parseOptions)
                      .withFallback(application)
                  catch
                    case cause: Exception =>
                      throw GraboidException.NotFound(s"Impossible to load $profile!", cause)
                }
    yield config
