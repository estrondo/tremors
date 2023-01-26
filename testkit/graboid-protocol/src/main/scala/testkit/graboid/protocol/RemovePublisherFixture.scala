package testkit.graboid.protocol

import graboid.protocol.RemovePublisher
import testkit.core._

object RemovePublisherFixture:

  def createRandom() = RemovePublisher(
    id = createRandomKey(16),
    publisherKey = createRandomKey(32)
  )
