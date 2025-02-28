package toph

import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import scala.reflect.ClassTag
import toph.context.TophExecutionContext
import zio.test.ZIOSpecDefault

abstract class TophSpec extends ZIOSpecDefault:

  export ArgumentMatchers.{any => anyOf}
  export ArgumentMatchers.{eq => eqTo}
  export Mockito.when

  given TophExecutionContext = TophExecutionContext.system(using ClassTag(getClass))

  def typeName[A: ClassTag]: String = summon[ClassTag[A]].runtimeClass.getSimpleName

//  override val bootstrap: ZLayer[Any, Any, TestEnvironment] =
//    Runtime.removeDefaultLoggers >>> SLF4J.slf4j >>> testEnvironment
