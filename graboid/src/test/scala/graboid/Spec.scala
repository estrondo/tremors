package graboid

import zio.Runtime
import zio.UIO
import zio.ULayer
import zio.ZIO
import zio.logging.backend.SLF4J
import zio.test.ZIOSpecDefault

abstract class Spec extends ZIOSpecDefault with SweetMockitoHelper:

  def logger: ULayer[Any] =
    Runtime.removeDefaultLoggers >>> SLF4J.slf4j
