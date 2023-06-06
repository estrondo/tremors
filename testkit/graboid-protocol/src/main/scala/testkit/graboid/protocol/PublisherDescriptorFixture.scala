package testkit.graboid.protocol

import graboid.protocol.PublisherDescriptor
import testkit.core.createRandomKey
import testkit.core.createRandomName
import testkit.core.createZonedDateTime

object PublisherDescriptorFixture:

  def createRandom() = PublisherDescriptor(
    key = createRandomKey(),
    name = createRandomName(),
    beginning = createZonedDateTime(),
    ending = Some(createZonedDateTime().plusDays(10)),
    location = s"http://${createRandomKey(64)}",
    `type` = "FDSN"
  )
