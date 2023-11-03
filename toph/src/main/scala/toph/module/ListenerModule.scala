package toph.module

import toph.centre.EventCentre
import toph.event.EventListener
import tremors.generator.KeyGenerator
import tremors.generator.KeyLength
import tremors.quakeml.Event
import tremors.zio.kafka.KafkaRouter
import tremors.zio.kafka.KConPro
import tremors.zio.kafka.KReader
import tremors.zio.kafka.KWriter
import tremors.zio.kafka.cbor.Borer
import zio.Task
import zio.ZIO
import zio.stream.ZStream

class ListenerModule(
    val event: ZStream[Any, Throwable, Event]
)

object ListenerModule:

  val GraboidEventTopic = "graboid.event"
  val TophEventTopic    = "toph.event"

  def apply(centreModule: CentreModule, kafkaModule: KafkaModule): Task[ListenerModule] =
    ZIO.succeed {
      new ListenerModule(
        event = subscribeGraboidEvent(centreModule.eventCentre, kafkaModule.router)
      )
    }

  private def subscribeGraboidEvent(centre: EventCentre, router: KafkaRouter): ZStream[Any, Throwable, Event] =
    given KReader[Event] = Borer.readerFor
    given KWriter[Event] = Borer.writerFor
    val eventListener    = EventListener(centre, KeyGenerator)

    router
      .subscribe(
        KConPro.AutoGeneratedKey(
          subscriptionTopic = GraboidEventTopic,
          productTopic = TophEventTopic,
          keyLength = KeyLength.Medium,
          mapper = (_, event: Event) => ZStream.fromZIO(eventListener(event))
        )
      )
