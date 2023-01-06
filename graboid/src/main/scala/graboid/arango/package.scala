package graboid.arango

import java.time.ZonedDateTime

def createZonedDateTime(): ZonedDateTime = ZonedDateTime.now(ArangoConversion.ZoneId)
