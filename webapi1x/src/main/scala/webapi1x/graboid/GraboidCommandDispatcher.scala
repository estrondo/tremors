package webapi1x.graboid

import com.softwaremill.macwire.wire
import core.KeyGenerator
import graboid.protocol.AddCrawler
import graboid.protocol.CrawlerDescriptor
import graboid.protocol.GraboidCommand
import io.bullet.borer.Cbor
import org.apache.kafka.clients.producer.ProducerRecord
import zio.RIO
import zio.Task
import zio.ULayer
import zio.ZIO
import zio.kafka.producer.Producer
import zio.kafka.serde.Serde

import GraboidCommandDispatcher.*

trait GraboidCommandDispatcher:

  def dispatch[A <: GraboidCommand](command: A): Task[CommandSent[A]]

object GraboidCommandDispatcher:

  val GraboidCommandTopic = "tremors.graboid-command"

  def apply(
      producerLayer: ULayer[Producer],
      keyGenerator: KeyGenerator
  ): Task[GraboidCommandDispatcher] = ZIO.attempt(wire[GraboidCommandDispatcherImpl])

  case class CommandSent[A <: GraboidCommand](key: String, command: A)

private[graboid] class GraboidCommandDispatcherImpl(
    producerLayer: ULayer[Producer],
    keyGenerator: KeyGenerator
) extends GraboidCommandDispatcher:

  override def dispatch[A <: GraboidCommand](command: A): Task[CommandSent[A]] =
    _createCrawler(command).provideLayer(producerLayer)

  private def _createCrawler[A <: GraboidCommand](
      command: A
  ): RIO[Producer, CommandSent[A]] =
    for
      bytes <- ZIO.attempt(Cbor.encode(command: GraboidCommand).toByteArray)
      key    = keyGenerator.next16()
      _     <- Producer.produce(GraboidCommandTopic, key, bytes, Serde.string, Serde.byteArray)
      _     <- ZIO.logInfo(s"It's been sent a CrawlerDescriptor with key=$key")
    yield CommandSent(key, command)
