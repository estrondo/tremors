package graboid

import zio.*
import zio.logging.backend.SLF4J

trait LoggerModule:

  def logger: UIO[ULayer[Any]]

object LoggerModule:

  def apply(): LoggerModule = new Impl()

  private class Impl extends LoggerModule:

    def logger: UIO[ULayer[Any]] =
      ZIO.succeed(Runtime.removeDefaultLoggers >>> SLF4J.slf4j)
