package graboid.fixture

import graboid.TimeWindow
import java.time.temporal.ChronoUnit

object TimeWindowFixture:

  def createRandom() = TimeWindow(
    beginning = createZonedDateTime(),
    ending = createZonedDateTime().plus(10, ChronoUnit.DAYS)
  )
