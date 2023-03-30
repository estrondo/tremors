package testkit.core

import java.time.Clock
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import scala.util.Random

private val Names = IndexedSeq(
  "olivia",
  "liam",
  "emma",
  "noah",
  "amelia",
  "oliver",
  "ava",
  "elijah",
  "sophia",
  "mateo"
)

private val Adjectives = IndexedSeq(
  "flamboyant",
  "chic",
  "eclectic",
  "modest",
  "stylish",
  "latest",
  "hot",
  "vintage"
)

private val Chars =
  IndexedSeq('1', '2', '3', '4', '5', '6', '7', '8', '9', '0', 'a', 'b', 'c', 'd', 'e', 'f')

def oneOf[T](values: IndexedSeq[T]): T =
  values(Random.nextInt(values.size))

def createRandomName(): String = s"${oneOf(Adjectives)}-${oneOf(Names)}"

def createRandomKey(length: Int = 8): String =

  val builder = StringBuilder()
  for _ <- 0 until length do builder.addOne(oneOf(Chars))
  builder.result()

def createZonedDateTime(): ZonedDateTime =
  ZonedDateTime.now(Clock.systemUTC()).truncatedTo(ChronoUnit.SECONDS)

def createRandomResourceID(): String =
  val schema      = oneOf(Array("smi", "quakeml"))
  val authorityId = createRandomKey(6)
  val resource    = createRandomKey(16)
  val localId     = createRandomKey(8)
  s"$schema://$authorityId/$resource#$localId"
