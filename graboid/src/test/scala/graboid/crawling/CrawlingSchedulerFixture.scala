package graboid.crawling

import java.time.Duration
import scala.util.Random

object CrawlingSchedulerFixture:

  object EventConfig:

    def createRandom(): CrawlingScheduler.EventConfig = CrawlingScheduler.EventConfig(
      interval = Duration.ofHours(2),
      queryWindow = Duration.ofMinutes(10),
      queries = Seq(
        CrawlingScheduler.EventQuery(
          magnitudeType = Some("abc"),
          minMagnitude = Some(Random.nextDouble() * 3d),
          maxMagnitude = Some(3d + Random.nextDouble() * 4d),
          eventType = Some("earthquake"),
        ),
      ),
    )
