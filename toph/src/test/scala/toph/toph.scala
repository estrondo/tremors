package toph

import java.time.ZonedDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import scala.util.Random

def createNewZonedDateTime(): ZonedDateTime =
  ZonedDateTime
    .now()
    .truncatedTo(ChronoUnit.MINUTES)
    .withZoneSameInstant(ZoneId.of("Z"))

def createRandomString(random: Random, length: Int): String =
  random.alphanumeric.take(length).mkString.toLowerCase
