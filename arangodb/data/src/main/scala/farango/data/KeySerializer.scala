package farango.data

import com.fasterxml.jackson.databind.ser.std.StdSerializer
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider

object KeySerializer extends StdSerializer[Key](classOf[Key]):

  override def serialize(value: Key, gen: JsonGenerator, provider: SerializerProvider): Unit =
    gen.writeString(Key.safe(value.value))
