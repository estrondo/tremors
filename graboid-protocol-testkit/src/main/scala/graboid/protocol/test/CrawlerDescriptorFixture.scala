package graboid.protocol.test

import graboid.protocol.CrawlerDescriptor
import testkit.createRandomKey
import testkit.createRandomName

import java.time.Clock
import java.time.Duration
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import scala.util.Random

object CrawlerDescriptorFixture:

  def createRandom() = CrawlerDescriptor(
    key = createRandomKey(),
    name = createRandomName(),
    `type` = "fdsn",
    source = createRandomKey(16),
    windowDuration = Duration.ofDays(10 + Random.nextLong(20)),
    starting = ZonedDateTime.now(Clock.systemUTC()).truncatedTo(ChronoUnit.SECONDS)
  )
