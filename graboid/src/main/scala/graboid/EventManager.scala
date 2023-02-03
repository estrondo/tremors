package graboid

import com.softwaremill.macwire.wire
import graboid.Crawler.Info
import graboid.Crawler.given
import graboid.kafka.GraboidDetectedEvent
import io.bullet.borer.Cbor
import org.apache.kafka.clients.producer.RecordMetadata
import zio.Task
import zio.TaskLayer
import zio.ZIO
import zio.kafka.producer.Producer
import zio.kafka.serde.Serde

trait EventManager:

  def register(info: Crawler.Info, publisher: Publisher, execution: CrawlerExecution): Task[Crawler.Info]

object EventManager:

  def apply(
      producerLayer: TaskLayer[Producer]
  ): EventManager =
    wire[Impl]

  private class Impl(
      producerLayer: TaskLayer[Producer]
  ) extends EventManager:

    def register(info: Info, publisher: Publisher, execution: CrawlerExecution): Task[Info] =
      for
        metadata <- produce(info)
        _        <- ZIO.logDebug(s"A new event has been sent to topic=${metadata.topic()}.")
      yield info

    private def produce(info: Info): Task[RecordMetadata] =
      for
        bytes    <- ZIO.fromTry(Cbor.encode(info).toByteArrayTry)
        metadata <-
          Producer
            .produce(GraboidDetectedEvent, s"event-${Crawler.getID(info)}", bytes, Serde.string, Serde.byteArray)
            .provideLayer(producerLayer)
      yield metadata
