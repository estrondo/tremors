package graboid.fixture

import core.KeyGenerator
import graboid.Crawler
import graboid.Publisher
import graboid.protocol.PublisherDescriptor
import zio.config.derivation.name

import java.net.URL
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import scala.util.Random

object PublisherFixture:

  def createRandom(): Publisher = Publisher(
    key = KeyGenerator.next4(),
    name = KeyGenerator.next8(),
    url = URL(s"http://${KeyGenerator.next4()}/"),
    beginning = createZonedDateTime(),
    ending = Some(createZonedDateTime().plus(10 + Random.nextInt(10), ChronoUnit.DAYS)),
    `type` = Crawler.Type.FDSN
  )

  def from(descriptor: PublisherDescriptor) = Publisher(
    key = descriptor.key,
    name = descriptor.name,
    url = URL(descriptor.location),
    beginning = descriptor.beginning,
    ending = descriptor.ending,
    `type` = Crawler.Type.valueOf(descriptor.`type`)
  )

  def updateFrom(descriptor: PublisherDescriptor) = Publisher.Update(
    name = descriptor.name,
    url = URL(descriptor.location),
    beginning = descriptor.beginning,
    ending = descriptor.ending,
    `type` = Crawler.Type.valueOf(descriptor.`type`)
  )

  def updateFrom(publisher: Publisher) = Publisher.Update(
    name = publisher.name,
    url = publisher.url,
    beginning = publisher.beginning,
    ending = publisher.ending,
    `type` = publisher.`type`
  )
