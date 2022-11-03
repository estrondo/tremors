package tremors.graboid.command

import graboid.protocol.CrawlerDescriptor
import tremors.graboid.createRandomKey
import tremors.graboid.createRandomString

import java.time.Clock
import java.time.Duration
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import scala.util.Random

object CrawlerDescriptorFixture:

  def createRandom() = CrawlerDescriptor(
    key = createRandomKey(),
    name = createRandomString(),
    `type` = "fdsn",
    source = createRandomString(),
    windowDuration = Duration.ofDays(10 + Random.nextLong(20)),
    starting = ZonedDateTime.now(Clock.systemUTC()).truncatedTo(ChronoUnit.SECONDS)
  )
