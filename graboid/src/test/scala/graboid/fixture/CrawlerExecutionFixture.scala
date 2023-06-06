package graboid.fixture

import core.KeyGenerator
import graboid.CrawlerExecution

object CrawlerExecutionFixture:

  def createRandom() = CrawlerExecution(
    key = KeyGenerator.next8(),
    publisherKey = KeyGenerator.next8(),
    beginning = createZonedDateTime(),
    ending = createZonedDateTime().plusDays(10),
    status = None,
    executionStarted = None,
    expectedStop = None,
    executionStopped = None,
    message = None
  )

  def createRandomSeq(num: Int) =
    for _ <- 0 until num yield createRandom()
