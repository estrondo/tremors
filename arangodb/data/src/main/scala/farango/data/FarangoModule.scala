package farango.data

import com.fasterxml.jackson.core.Version
import com.fasterxml.jackson.databind.module.SimpleModule
import java.time.ZonedDateTime
import scala.jdk.CollectionConverters.MapHasAsJava
import scala.jdk.CollectionConverters.SeqHasAsJava

object FarangoModule
    extends SimpleModule(
      "FarangoModule",
      Version.unknownVersion(),
      Map(
        classOf[ZonedDateTime] -> ZonedDateTimeDeserializer,
        classOf[Key]           -> KeyDeserializer
      ).asJava,
      Seq(
        ZonedDateTimeSerializer,
        KeySerializer
      ).asJava
    )
