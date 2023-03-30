package graboid

import farango.zio.starter.ArangoConfig
import farango.zio.starter.ArangoHost
import graboid.config.GraboidConfig
import zio.RIO
import zio.Task
import zio.ZIO
import zio.ZIOAppArgs
import zio.config.magnolia.*
import zioapp.ZProfile
import zioapp.ZProfile.given

trait ConfigModule:

  def config: RIO[ZIOAppArgs, GraboidConfig]

object ConfigModule:

  def apply(): ConfigModule = new Impl()

  private class Impl extends ConfigModule:

    case class C(graboid: GraboidConfig)

    def config: RIO[ZIOAppArgs, GraboidConfig] =
      for
        tuple            <- ZProfile
                              .load[C](useFirstArgumentLine = true)
                              .orDieWith(GraboidException.IllegalState("It's impossible to start Graboid!", _))
        (config, profile) = tuple
        _                <- profile match
                              case Some(profile) => ZIO.logInfo(s"Graboid has been started in [$profile].")
                              case None          => ZIO.logInfo("Graboid has been started in default mode.")
      yield config.graboid
