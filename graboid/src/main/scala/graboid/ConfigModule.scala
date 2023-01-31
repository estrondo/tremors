package graboid

import graboid.config.ArangoConfig
import graboid.config.ArangoHost
import graboid.config.GraboidConfig
import zio.Task
import zio.ZIO
import zio.RIO
import zio.config.magnolia.*
import zioapp.ZProfile
import zio.ZIOAppArgs

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

    private def toArangoHost(value: String): Seq[ArangoHost] =
      for part <- value.split("\\s*,\\s*")
      yield part.split(":") match
        case Array(hostname, port) => ArangoHost(hostname, port.toInt)
        case _                     => throw IllegalArgumentException(value)

    private def fromArangoHost(values: Seq[ArangoHost]): String = throw IllegalStateException(
      "fromArangoHost"
    )

    private given seqArangoHost: Descriptor[Seq[ArangoHost]] =
      Descriptor.from(Descriptor[String].transform(toArangoHost, fromArangoHost))

    private given seqString: Descriptor[Seq[String]] =
      Descriptor.from(
        Descriptor[String].transform(string => string.split("""\s*,\s*""").toSeq, _.mkString(", "))
      )
