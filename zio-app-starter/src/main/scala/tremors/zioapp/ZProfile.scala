package tremors.zioapp

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import com.typesafe.config.ConfigParseOptions
import com.typesafe.config.ConfigSyntax
import zio.System
import zio.Task
import zio.ZIO
import zio.config.magnolia.Descriptor
import zio.config.magnolia.descriptor
import zio.config.read
import zio.config.toKebabCase
import zio.config.typesafe.TypesafeConfigSource

object ZProfile:

  private def parseOptions: ConfigParseOptions =
    ConfigParseOptions
      .defaults()
      .setSyntax(ConfigSyntax.CONF)
      .setAllowMissing(false)

  def load[T: Descriptor](
      profilePropertyName: String = "tremors.profile",
      profileEnvName: String = "TREMORS_PROFILE",
      resourcePattern: String = "application-[profile].conf",
      defaultApplication: String = "application.conf"
  ): Task[(T, Option[String])] =
    for
      propertyProfile <- System.property(profilePropertyName)
      envProfile      <- System.env(profileEnvName)
      userProfile      = propertyProfile.orElse(envProfile).map(_.toLowerCase())
      config          <- userProfile match
                           case Some(profile) =>
                             loadProfile(profile, resourcePattern, defaultApplication)
                           case _             =>
                             ZIO.attemptBlocking(loadResource(defaultApplication))
      parsed          <- parse(config)
    yield (parsed, userProfile)

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
    ZIO.attemptBlocking {
      loadResource(resourcePattern.replace("[profile]", profile))
        .withFallback(loadResource(defaultApplication))
    }
