package testkit.graboid.protocol

import graboid.protocol.PublisherDescriptor
import io.bullet.borer.derivation.key
import testkit.core.createRandomKey
import testkit.core.createRandomName
import testkit.core.createZonedDateTime
import testkit.core.oneOf
import java.time.temporal.ChronoUnit

object PublisherDescriptorFixture:

  def createRandom() = PublisherDescriptor(
    key = createRandomKey(),
    name = createRandomName(),
    beginning = createZonedDateTime(),
    ending = Some(createZonedDateTime().plusDays(10)),
    location = s"http://${createRandomKey(64)}",
    `type` = "FDSN"
  )
