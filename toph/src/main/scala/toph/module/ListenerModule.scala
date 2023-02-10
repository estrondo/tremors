package toph.module

import com.softwaremill.macwire.wire
import zio.{Task, ZIO}
import zio.stream.ZStream
import toph.message.protocol.EventJournalMessage
import toph.listener.EventListener
import toph.publisher.EventPublisher
import toph.kafka.GraboidDetectedEventTopic
import cbor.quakeml.given

trait ListenerModule:

  val eventJournalStream: Task[ZStream[Any, Throwable, EventJournalMessage]]

object ListenerModule:

  def apply(coreModule: CoreModule, kafkaModule: KafkaModule): Task[ListenerModule] =
    ZIO.attempt(wire[Impl])

  private class Impl(coreModule: CoreModule, kafkaModule: KafkaModule) extends ListenerModule:

    private val eventListener  = EventListener(coreModule.eventManager)
    private val eventPublisher = EventPublisher()

    override val eventJournalStream: Task[ZStream[Any, Throwable, EventJournalMessage]] =
      for stream <- kafkaModule.manager.subscribe(GraboidDetectedEventTopic, eventListener, eventPublisher)
      yield stream.flatMap(x => ZStream.fromIterable(x._2))
