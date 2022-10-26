package tremors.graboid.command

import tremors.graboid.CrawlerDescriptor
import tremors.graboid.{createRandomString, createRandomKey}

import java.time.Duration
import java.time.ZonedDateTime
import scala.util.Random
import java.time.Clock
import java.time.temporal.ChronoUnit

object CrawlerDescriptorFixture:

  def createRandom() = CrawlerDescriptor(
    key = createRandomKey(),
    name = createRandomString(),
    `type` = "fdsn",
    source = createRandomString(),
    windowDuration = Duration.ofDays(10 + Random.nextLong(20)),
    starting = ZonedDateTime.now(Clock.systemUTC()).truncatedTo(ChronoUnit.SECONDS)
  )
