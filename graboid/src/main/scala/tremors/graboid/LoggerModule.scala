package tremors.graboid

import zio._
import zio.logging.backend.SLF4J

object LoggerModule:

  def apply(): LoggerModule = LoggerModuleImpl()

trait LoggerModule:

  def logger: UIO[ULayer[Any]]

private class LoggerModuleImpl extends LoggerModule:

  def logger: UIO[ULayer[Any]] =
    ZIO.succeed(Runtime.removeDefaultLoggers >>> SLF4J.slf4j)
