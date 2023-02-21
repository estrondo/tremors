package toph.converter

import zio.Task
import toph.message.protocol.NewEvent
import toph.model.data.EventData
import toph.model.data.HypocentreData
import zio.ZIO

object EventJournalMessageConverter:

  def newEventFrom(event: EventData, hypocentres: Seq[HypocentreData]): Task[NewEvent] =
    ZIO.attempt {
      NewEvent(event.key)
    }
