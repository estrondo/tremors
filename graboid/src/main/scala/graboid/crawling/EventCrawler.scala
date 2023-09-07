package graboid.crawling

import graboid.DataCentre
import graboid.GraboidException
import tremors.quakeml.Event
import zio.RIO
import zio.ZIO
import zio.http.Client
import zio.kafka.producer.Producer
import zio.stream.ZStream

trait EventCrawler:

  def apply(query: EventCrawlingQuery): ZStream[Client & Producer, Throwable, EventCrawler.FoundEvent]

object EventCrawler:

  val GraboidEventTopic = "graboid.event"

  def apply(dataCentre: DataCentre): RIO[Client, EventCrawler] =
    dataCentre.event match
      case Some(event) => ZIO.succeed(Impl(dataCentre, event))
      case None        => ZIO.fail(GraboidException.CrawlingException(s"DataCentre ${dataCentre.id} has no event URL!"))

  trait Factory:
    def apply(dataCentre: DataCentre): RIO[Client, EventCrawler]

  case class FoundEvent(dataCentre: DataCentre, event: Event)

  private class Impl(dataCentre: DataCentre, event: String) extends EventCrawler:

    override def apply(query: EventCrawlingQuery): ZStream[Client & Producer, Throwable, FoundEvent] = ???


