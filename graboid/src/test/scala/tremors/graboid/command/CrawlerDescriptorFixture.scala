package tremors.graboid.command

import tremors.graboid.CrawlerDescriptor
import tremors.graboid.createRandomString

import java.time.Duration
import java.time.ZonedDateTime
import scala.util.Random

object CrawlerDescriptorFixture:

  def createRandom() = CrawlerDescriptor(
    name = createRandomString(),
    `type` = "fdsn",
    source = createRandomString(),
    windowDuration = Duration.ofDays(10 + Random.nextLong(20)),
    starting = ZonedDateTime.now()
  )
