package graboid.fixture

import java.time.Clock
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import scala.util.Random

extension (random: Random)
  def oneOf[T](indexed: IndexedSeq[T]): T =
    indexed(random.nextInt(indexed.length))

def createRandomKey(length: Int = 8): String =
  val validChars = Array('1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f')
  val builder    = new StringBuilder()
  for _ <- 0 until length do builder.addOne(Random.oneOf(validChars))
  builder.result()

def createRandomString(length: Int = 8): String =
  val builder = new StringBuilder()
  for _ <- 0 until length do builder.append(Random.nextPrintableChar())
  builder.result()

def createZonedDateTime(): ZonedDateTime =
  ZonedDateTime
    .now()
    .truncatedTo(ChronoUnit.SECONDS)
    .withZoneSameInstant(Clock.systemUTC().getZone())
