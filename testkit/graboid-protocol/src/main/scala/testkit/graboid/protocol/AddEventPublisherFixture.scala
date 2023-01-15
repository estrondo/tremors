package testkit.graboid.protocol

import graboid.protocol.AddEventPublisher
import testkit.core.createRandomKey

object AddEventPublisherFixture:

  def createRandom() = AddEventPublisher(
    id = createRandomKey(),
    descriptor = EventPublisherDescriptorFixture.createRandom()
  )
