package graboid.fixture

import core.KeyGenerator
import graboid.TimeWindow

import java.time.temporal.ChronoUnit
import scala.util.Random

object TimeWindowFixture:

  def createRandom() = TimeWindow(
    key = KeyGenerator.next4(),
    publisherKey = KeyGenerator.next4(),
    beginning = createZonedDateTime(),
    ending = createZonedDateTime().plus(10, ChronoUnit.DAYS),
    successes = Random.nextInt(1000),
    failures = Random.nextInt(10)
  )
