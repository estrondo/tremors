package testkit.graboid.protocol

import graboid.protocol.RunAllPublishers
import testkit.core.createRandomKey

object RunAllPublishersFixture:

  def createRandom() = RunAllPublishers(
    id = createRandomKey(16)
  )
