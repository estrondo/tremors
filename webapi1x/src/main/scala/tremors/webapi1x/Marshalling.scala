package tremors.webapi1x

import zhttp.http.Request
import zio.IO
import zio.Task
import zio.json.{given, *}
import zio.stream.ZSink
import scala.collection.mutable
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import zio.ZIO

object Marshalling:

  val DefaultMaxLength = 8 * 1024

  def extractJSON[T: JsonDecoder](request: Request, maxLength: Int = DefaultMaxLength): Task[T] =
    for entity <- request.body.asStream
                    .run(ZSink.collectAllN(DefaultMaxLength))
                    .map(chunk => String(chunk.toArray, StandardCharsets.UTF_8))
                    .flatMap(json => mapToZIO(json.fromJson[T]))
    yield entity

  private def mapToZIO[T](either: Either[String, T]): Task[T] =
    either match
      case Right(value)  => ZIO.succeed(value)
      case Left(message) => ZIO.fail(IllegalArgumentException(message))
