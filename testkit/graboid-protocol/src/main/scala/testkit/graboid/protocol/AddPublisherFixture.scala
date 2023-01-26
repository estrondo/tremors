package testkit.graboid.protocol

import graboid.protocol.AddPublisher
import testkit.core.createRandomKey

object AddPublisherFixture:

  def createRandom() = AddPublisher(
    id = createRandomKey(),
    descriptor = PublisherDescriptorFixture.createRandom()
  )
