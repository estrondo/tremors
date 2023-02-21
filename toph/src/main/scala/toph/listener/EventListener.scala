package toph.listener

import com.softwaremill.macwire.wire
import quakeml.QuakeMLDetectedEvent
import toph.manager.EventDataManager
import toph.model.data.EventData
import toph.model.data.HypocentreData
import toph.model.data.MagnitudeData
import zio.Task
import zkafka.KafkaSubscriber

trait EventListener extends KafkaSubscriber[QuakeMLDetectedEvent, (EventData, Seq[HypocentreData], Seq[MagnitudeData])]

object EventListener:

  def apply(manager: EventDataManager): EventListener =
    wire[Impl]

  private class Impl(manager: EventDataManager) extends EventListener:

    override def accept(
        key: String,
        value: QuakeMLDetectedEvent
    ): Task[Option[(EventData, Seq[HypocentreData], Seq[MagnitudeData])]] =
      for result <- manager.accept(value)
      yield Some(result)
