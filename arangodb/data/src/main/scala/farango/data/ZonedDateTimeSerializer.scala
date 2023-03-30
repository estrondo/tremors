package farango.data

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import java.time.Clock
import java.time.ZonedDateTime
import java.time.temporal.ChronoField

object ZonedDateTimeSerializer extends StdSerializer[ZonedDateTime](classOf[ZonedDateTime]):

  override def serialize(value: ZonedDateTime, gen: JsonGenerator, provider: SerializerProvider): Unit =
    gen.writeNumber(value.getLong(ChronoField.INSTANT_SECONDS))
