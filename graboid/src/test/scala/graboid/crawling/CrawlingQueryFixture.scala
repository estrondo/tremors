package graboid.crawling

import scala.util.Random
import tremors.ZonedDateTimeFixture
import tremors.generator.KeyGenerator
import tremors.generator.KeyLength

object CrawlingQueryFixture:

  def createRandom(): EventCrawlingQuery =
    val startTime = ZonedDateTimeFixture.createRandom()
    EventCrawlingQuery(
      schedulingId = KeyGenerator.generate(KeyLength.Medium),
      starting = startTime,
      ending = startTime.minusHours(3),
      maxMagnitude = Some(Random.nextDouble() * 7),
      minMagnitude = Some(Random.nextDouble() * 2)
    )
