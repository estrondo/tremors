package toph.model.objectstorage

import com.google.protobuf.ByteString
import io.github.arainko.ducktape.*
import java.time.ZonedDateTime

given Transformer[Option[String], String] = {
  case Some(value) => value
  case None        => ""
}

given Transformer[ZonedDateTime, Long] = _.toEpochSecond

given Transformer[Array[Byte], ByteString] = ByteString.copyFrom(_)

given Transformer[ByteString, Array[Byte]] = _.toByteArray
