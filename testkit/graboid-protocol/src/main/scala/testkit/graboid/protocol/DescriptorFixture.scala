package testkit.graboid.protocol

import graboid.protocol.CrawlerDescriptor
import graboid.protocol.UpdateCrawlerDescriptor
import testkit.core.createRandomKey
import testkit.core.createRandomName

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

object UpdateCrawlerDescriptorFixture:

  def createRandom() = UpdateCrawlerDescriptor(
    name = Some(createRandomName()),
    `type` = Some("fdsn"),
    source = Some(createRandomKey(32)),
    windowDuration = Some(Duration.ofDays(5 + Random.nextLong(5))),
    starting = Some(ZonedDateTime.now(Clock.systemUTC()).truncatedTo(ChronoUnit.SECONDS))
  )
