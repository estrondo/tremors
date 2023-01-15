package testkit.graboid.protocol

import graboid.protocol.UpdateEventPublisher
import testkit.core.*

object UpdateEventPublisherFixture:
  def createRandom() = UpdateEventPublisher(
    id = createRandomKey(16),
    descriptor = EventPublisherDescriptorFixture.createRandom()
  )
