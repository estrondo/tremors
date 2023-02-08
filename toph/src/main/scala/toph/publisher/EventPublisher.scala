package toph.publisher

import toph.model.Epicentre
import toph.model.Event
import toph.model.Hypocentre
import zio.Task

trait EventPublisher:

  def publish(event: Event, origins: Seq[(Epicentre, Option[Hypocentre])]): Task[(Event, Seq[(Epicentre, Hypocentre)])]
