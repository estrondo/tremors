package tremors.logging

import ch.qos.logback.classic.spi.Configurator
import ch.qos.logback.classic.spi.Configurator.ExecutionStatus
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.core.Context
import ch.qos.logback.core.status.Status
import ch.qos.logback.core.spi.ContextAware
import ch.qos.logback.core.spi.ContextAwareBase
import ch.qos.logback.core.util.Loader
import ch.qos.logback.classic.util.DefaultJoranConfigurator

class TremorsLoggingConfigurator extends DefaultJoranConfigurator with Configurator:

  override def configure(loggerContext: LoggerContext): ExecutionStatus =
    val classLoader = Loader.getClassLoaderOfObject(this)

    val systemProfile = Option(System.getProperty("tremors.profile"))
    val envProfile    = Option(System.getenv().get("TREMORS_PROFILE"))

    val fileName = systemProfile.orElse(envProfile) match
      case Some(profile) => s"logack-$profile.xml"
      case None          => "logback.xml"

    val url = classLoader.getResource(fileName)
    if (url != null) {
      configureByResource(url)
      ExecutionStatus.DO_NOT_INVOKE_NEXT_IF_ANY
    } else {
      println(s"There is no $fileName!")
      ExecutionStatus.INVOKE_NEXT_IF_ANY
    }
