package testkit.graboid.protocol

import graboid.protocol.GraboidCommandResult
import scala.util.Random
import testkit.core.*

object GraboidCommandResultFixture:

  def createRandom() = GraboidCommandResult(
    id = createRandomKey(),
    time = Random.nextLong(10000),
    status = GraboidCommandResult.ok(createRandomKey(32))
  )
