package graboid

import zio.Runtime
import zio.ULayer
import zio.logging.backend.SLF4J
import zio.test.ZIOSpecDefault

abstract class Spec extends ZIOSpecDefault:

  def logger: ULayer[Any] =
    Runtime.removeDefaultLoggers >>> SLF4J.slf4j
