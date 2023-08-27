package graboid.crawling

import scala.util.Random
import tremors.ZonedDateTimeFixture

object CrawlingQueryFixture:

  def createRandom(): CrawlingQuery =
    val startTime = ZonedDateTimeFixture.createRandom()
    CrawlingQuery(
      starting = startTime,
      ending = startTime.minusHours(3),
      maxMagnitude = Some(Random.nextDouble() * 7),
      minMagnitude = Some(Random.nextDouble() * 2)
    )
