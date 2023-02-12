package farango.data

import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.core.JsonParser

object KeyDeserializer extends StdDeserializer[Key](classOf[Key]):

  override def deserialize(p: JsonParser, ctxt: DeserializationContext): Key =
    Key(Key.unsafe(p.getText()))
