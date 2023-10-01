package graboid.crawling

import java.time.Duration
import scala.util.Random
import tremors.ZonedDateTimeFixture

object EventCrawlingQueryFixture:

  def createRandom(owner: EventCrawlingQuery.Owner): EventCrawlingQuery =
    val startTime = ZonedDateTimeFixture.createRandom()
    EventCrawlingQuery(
      starting = startTime,
      ending = startTime.plusDays(3),
      timeWindow = Duration.ofMinutes(Random.nextInt(60)),
      owner = owner,
      queries = Seq(
        EventCrawlingQuery.Query(
          magnitudeType = Some("abc"),
          eventType = Some("earthquake"),
          min = Some(Random.nextDouble() * 5),
          max = None
        )
      )
    )
