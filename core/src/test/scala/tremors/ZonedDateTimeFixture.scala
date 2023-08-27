package tremors

import java.time.Clock
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import scala.util.Random

object ZonedDateTimeFixture:

  def createRandom(): ZonedDateTime =
    ZonedDateTime
      .now(Clock.systemUTC())
      .plusSeconds(Random.nextInt(48 * 60) - 24 * 60)
      .truncatedTo(ChronoUnit.SECONDS)
