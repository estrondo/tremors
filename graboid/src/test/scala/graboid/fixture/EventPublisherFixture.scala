package graboid.fixture

import core.KeyGenerator
import graboid.Crawler
import graboid.EventPublisher
import graboid.protocol.EventPublisherDescriptor
import zio.config.derivation.name

import java.net.URL
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import scala.util.Random

object EventPublisherFixture:

  def createRandom(): EventPublisher = EventPublisher(
    key = KeyGenerator.next4(),
    name = KeyGenerator.next8(),
    url = URL(s"http://${KeyGenerator.next4()}/"),
    beginning = createZonedDateTime(),
    ending = Some(createZonedDateTime().plus(10 + Random.nextInt(10), ChronoUnit.DAYS)),
    `type` = Crawler.Type.FDSN
  )

  def from(descriptor: EventPublisherDescriptor) = EventPublisher(
    key = descriptor.key,
    name = descriptor.name,
    url = URL(descriptor.location),
    beginning = descriptor.beginning,
    ending = descriptor.ending,
    `type` = Crawler.Type.valueOf(descriptor.`type`)
  )

  def updateFrom(descriptor: EventPublisherDescriptor) = EventPublisher.Update(
    name = descriptor.name,
    url = URL(descriptor.location),
    beginning = descriptor.beginning,
    ending = descriptor.ending,
    `type` = Crawler.Type.valueOf(descriptor.`type`)
  )

  def updateFrom(eventPublisher: EventPublisher) = EventPublisher.Update(
    name = eventPublisher.name,
    url = eventPublisher.url,
    beginning = eventPublisher.beginning,
    ending = eventPublisher.ending,
    `type` = eventPublisher.`type`
  )
