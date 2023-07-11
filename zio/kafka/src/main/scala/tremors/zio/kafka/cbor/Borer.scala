package tremors.zio.kafka.cbor

import io.bullet.borer.Cbor
import io.bullet.borer.Decoder
import io.bullet.borer.Encoder
import io.bullet.borer.derivation.ArrayBasedCodecs.deriveDecoder
import io.bullet.borer.derivation.ArrayBasedCodecs.deriveEncoder
import tremors.zio.kafka.KReader
import tremors.zio.kafka.KWriter
import zio.Task
import zio.ZIO

object Borer:

  inline def reader[A]: KReader[A] =
    given decoder: Decoder[A] = deriveDecoder
    new KReader[A]:
      override def apply(bytes: Array[Byte]): Task[A] =
        ZIO.fromTry(Cbor.decode(bytes).to[A].valueTry)

  inline def writer[A]: KWriter[A] =
    given encoder: Encoder[A] = deriveEncoder
    new KWriter[A]:
      override def apply(value: A): Task[Array[Byte]] =
        ZIO.fromTry(Cbor.encode(value).toByteArrayTry)
