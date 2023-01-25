package graboid.arango

import java.time.ZonedDateTime
import farango.data.ArangoConversion

def createZonedDateTime(): ZonedDateTime = ZonedDateTime.now(ArangoConversion.ZoneId)
