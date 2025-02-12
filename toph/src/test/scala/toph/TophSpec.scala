package toph

import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import toph.context.TophExecutionContext
import zio.test.ZIOSpecDefault

import scala.reflect.ClassTag

abstract class TophSpec extends ZIOSpecDefault:

  export ArgumentMatchers.{any => anyOf}
  export ArgumentMatchers.{eq => eqTo}
  export Mockito.when

  given TophExecutionContext = TophExecutionContext.system(using ClassTag(getClass))

//  override val bootstrap: ZLayer[Any, Any, TestEnvironment] =
//    Runtime.removeDefaultLoggers >>> SLF4J.slf4j >>> testEnvironment
