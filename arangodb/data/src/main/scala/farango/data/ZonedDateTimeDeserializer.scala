package farango.data

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.Deserializers
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import java.time.Clock
import java.time.Instant
import java.time.ZonedDateTime
import java.time.ZoneId

object ZonedDateTimeDeserializer extends StdDeserializer[ZonedDateTime](classOf[ZonedDateTime]):

  val ZoneId: ZoneId = Clock.systemUTC().getZone()

  override def deserialize(p: JsonParser, ctxt: DeserializationContext): ZonedDateTime =
    ZonedDateTime.ofInstant(Instant.ofEpochSecond(p.getNumberValue().longValue()), ZoneId)
