package graboid.fixture

import core.KeyGenerator
import graboid.TimeWindow

import java.time.temporal.ChronoUnit
import scala.util.Random

object TimeWindowFixture:

  def createRandom() = TimeWindow(
    beginning = createZonedDateTime(),
    ending = createZonedDateTime().plus(10, ChronoUnit.DAYS)
  )
