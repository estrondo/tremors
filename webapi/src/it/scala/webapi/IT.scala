package webapi

import zio.test.TestEnvironment

import zio.ZLayer
import zio.Runtime
import zio.logging.backend.SLF4J
import zio.test.testEnvironment

abstract class IT extends zio.test.ZIOSpecDefault:

  override val bootstrap: ZLayer[Any, Any, TestEnvironment] =
    Runtime.removeDefaultLoggers >>> SLF4J.slf4j >>> testEnvironment
