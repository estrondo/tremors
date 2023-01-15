package testkit.graboid.protocol

import graboid.protocol.RemoveEventPublisher
import testkit.core._

object RemoveEventPublisherFixture:

  def createRandom() = RemoveEventPublisher(
    id = createRandomKey(16),
    publisherKey = createRandomKey(32)
  )
