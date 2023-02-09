package toph.listener

import com.softwaremill.macwire.wire
import zkafka.KafkaSubscriber
import quakeml.DetectedEvent
import toph.manager.EventManager
import zio.Task
import toph.model.Epicentre
import toph.model.Hypocentre
import toph.model.Event

trait EventListener extends KafkaSubscriber[DetectedEvent, (Event, Seq[(Epicentre, Option[Hypocentre])])]

object EventListener:

  def apply(manager: EventManager): EventListener =
    wire[Impl]

  private class Impl(manager: EventManager) extends EventListener:

    override def accept(
        key: String,
        value: DetectedEvent
    ): Task[Option[(Event, Seq[(Epicentre, Option[Hypocentre])])]] =
      for result <- manager.accept(value)
      yield Some(result)
