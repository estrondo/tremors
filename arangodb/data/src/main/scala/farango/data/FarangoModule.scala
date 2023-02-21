package farango.data

import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.core.Version
import java.time.ZonedDateTime
import scala.jdk.CollectionConverters.{MapHasAsJava, SeqHasAsJava}

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
