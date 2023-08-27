package graboid

import org.mockito.ArgumentMatchers
import zio.Runtime
import zio.ZLayer
import zio.logging.backend.SLF4J
import zio.stream.ZStream
import zio.test.TestEnvironment
import zio.test.ZIOSpecDefault
import zio.test.testEnvironment

abstract class GraboidSpec extends ZIOSpecDefault:

  override val bootstrap: ZLayer[Any, Any, TestEnvironment] =
    Runtime.removeDefaultLoggers >>> SLF4J.slf4j >>> testEnvironment

  protected def any[T](): T = ArgumentMatchers.any()

  protected def eqTo[T](expected: T): T = ArgumentMatchers.eq(expected)

  protected def readFile(path: String): ZStream[Any, Throwable, Byte] =
    ZStream.fromFileName(path)
