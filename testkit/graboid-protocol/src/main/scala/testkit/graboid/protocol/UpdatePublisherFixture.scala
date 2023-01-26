package testkit.graboid.protocol

import graboid.protocol.UpdatePublisher
import testkit.core.*

object UpdatePublisherFixture:
  def createRandom() = UpdatePublisher(
    id = createRandomKey(16),
    descriptor = PublisherDescriptorFixture.createRandom()
  )
