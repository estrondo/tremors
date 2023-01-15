package testkit.graboid.protocol

import graboid.protocol.GraboidCommandResult

import testkit.core.*
import scala.util.Random

object GraboidCommandResultFixture:

  def createRandom() = GraboidCommandResult(
    id = createRandomKey(),
    time = Random.nextLong(10000),
    status = GraboidCommandResult.Ok(createRandomKey(32))
  )
