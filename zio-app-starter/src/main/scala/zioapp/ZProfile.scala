package zioapp

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import com.typesafe.config.ConfigParseOptions
import com.typesafe.config.ConfigSyntax
import zio.System
import zio.Task
import zio.RIO
import zio.ZIO
import zio.URIO
import zio.config.magnolia.Descriptor
import zio.config.magnolia.descriptor
import zio.config.read
import zio.config.toKebabCase
import zio.config.typesafe.TypesafeConfigSource
import zio.ZIOAppArgs

object ZProfile:

  val DefaultProfilePropertyName = "tremors.profile"
  val DefaultProfileEnvName      = "TREMORS_PROFILE"
  val DefaultResourcePattern     = "application-[profile].conf"
  val DefaultDefaultApplication  = "application.conf"

  private def parseOptions: ConfigParseOptions =
    ConfigParseOptions
      .defaults()
      .setSyntax(ConfigSyntax.CONF)
      .setAllowMissing(false)

  def loadOnlyConfig[T: Descriptor](
      profilePropertyName: String = DefaultProfilePropertyName,
      profileEnvName: String = DefaultProfileEnvName,
      resourcePattern: String = DefaultResourcePattern,
      defaultApplication: String = DefaultDefaultApplication,
      useFirstArgumentLine: Boolean
  ): RIO[ZIOAppArgs, T] =
    for config <- load(profilePropertyName, profileEnvName, resourcePattern, defaultApplication, useFirstArgumentLine)
    yield config._1

  def load[T: Descriptor](
      profilePropertyName: String = DefaultProfilePropertyName,
      profileEnvName: String = DefaultProfileEnvName,
      resourcePattern: String = DefaultResourcePattern,
      defaultApplication: String = DefaultDefaultApplication,
      useFirstArgumentLine: Boolean
  ): RIO[ZIOAppArgs, (T, Option[String])] =
    for
      propertyProfile <- System.property(profilePropertyName)
      envProfile      <- System.env(profileEnvName)
      userProfile     <- getUserProfile(useFirstArgumentLine, propertyProfile, envProfile)
      config          <- userProfile match
                           case Some(profile) =>
                             loadProfile(profile, resourcePattern, defaultApplication)
                           case _             =>
                             ZIO.attemptBlocking(loadResource(defaultApplication))
      parsed          <- parse(config)
    yield (parsed, userProfile)

  private def getUserProfile(
      userFirstArgument: Boolean,
      propertyProfile: Option[String],
      envProfile: Option[String]
  ): URIO[ZIOAppArgs, Option[String]] =
    val fallback = propertyProfile.orElse(envProfile)
    if !userFirstArgument then ZIO.succeed(fallback)
    else
      for args <- ZIOAppArgs.getArgs
      yield args.headOption.orElse(fallback)

  private def parse[T: Descriptor](config: Config): Task[T] =
    val source = TypesafeConfigSource.fromTypesafeConfig(ZIO.attemptBlocking(config.resolve()))
    read(descriptor[T].mapKey(toKebabCase).from(source))

  private def loadResource(resource: String): Config =
    ConfigFactory.parseResources(resource, parseOptions)

  private def loadProfile(
      profile: String,
      resourcePattern: String,
      defaultApplication: String
  ): Task[Config] =
    ZIO.logInfo(s"Attempt load profile=$profile.") *> ZIO.attemptBlocking {
      loadResource(resourcePattern.replace("[profile]", profile))
        .withFallback(loadResource(defaultApplication))
    }
