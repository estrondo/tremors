package toph.converter

import toph.model.Event
import toph.model.Epicentre
import toph.model.Hypocentre
import zio.Task
import toph.message.protocol.NewEvent
import zio.ZIO

object EventJournalMessageConverter:

  def newEventFrom(event: Event, origins: Seq[(Epicentre, Option[Hypocentre])]): Task[NewEvent] =
    ZIO.attempt {
      NewEvent(event.key)
    }
