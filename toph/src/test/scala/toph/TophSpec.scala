package toph

import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import toph.context.TophExecutionContext
import zio.Runtime
import zio.ZLayer
import zio.logging.backend.SLF4J
import zio.test.TestEnvironment
import zio.test.ZIOSpecDefault
import zio.test.testEnvironment

abstract class TophSpec extends ZIOSpecDefault:

  export ArgumentMatchers.{any => anyOf}
  export ArgumentMatchers.{eq => eqTo}
  export Mockito.when

  given TophExecutionContext = TophExecutionContext.systemUser("Automatic Tester")

//  override val bootstrap: ZLayer[Any, Any, TestEnvironment] =
//    Runtime.removeDefaultLoggers >>> SLF4J.slf4j >>> testEnvironment
