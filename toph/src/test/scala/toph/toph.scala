package toph

import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

def createNewZonedDateTime(): ZonedDateTime =
  ZonedDateTime.now().truncatedTo(ChronoUnit.MINUTES)
