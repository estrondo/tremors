package tremors.zio.kafka.cbor

import io.bullet.borer.Cbor
import io.bullet.borer.Decoder
import io.bullet.borer.Encoder
import io.bullet.borer.derivation.ArrayBasedCodecs.deriveDecoder
import io.bullet.borer.derivation.ArrayBasedCodecs.deriveEncoder
import tremors.zio.kafka.KReader
import tremors.zio.kafka.KWriter
import zio.ZIO

object Borer:

  inline def reader[A]: KReader[A] =
    given decoder: Decoder[A] = deriveDecoder
    (bytes: Array[Byte]) => ZIO.fromTry(Cbor.decode(bytes).to[A].valueTry)

  inline def writer[A]: KWriter[A] =
    given encoder: Encoder[A] = deriveEncoder
    (value: A) => ZIO.fromTry(Cbor.encode(value).toByteArrayTry)

  def readerFor[A: Decoder]: KReader[A] =
    (bytes: Array[Byte]) => ZIO.from(Cbor.decode(bytes).to[A].valueTry)

  def writerFor[A: Encoder]: KWriter[A] =
    (value: A) => ZIO.fromTry(Cbor.encode(value).toByteArrayTry)
