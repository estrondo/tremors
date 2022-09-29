package tremors.graboid

import tremors.graboid.quakeml.model.Event
import tremors.graboid.quakeml.model.Magnitude
import tremors.graboid.quakeml.model.Origin
import zio.Task
import zio.stream.ZStream

import java.time.ZonedDateTime
import tremors.graboid.quakeml.model.CreationInfo

object Crawler:

  type Info     = Event | Origin | Magnitude
  type Stream   = ZStream[Any, Throwable, Crawler.Info]
  type Interval = (Option[ZonedDateTime], Option[ZonedDateTime])

  extension (interval: Interval)
    def contains(creationInfo: Option[CreationInfo]): Boolean = creationInfo match
      case Some(CreationInfo(_, _, _, _, Some(dateTime), _)) =>
        interval match
          case (Some(start), Some(end)) =>
            dateTime.compareTo(start) >= 0 && dateTime.compareTo(end) <= 0
          case (Some(start), _)         => dateTime.compareTo(start) >= 0
          case (_, Some(end))           => dateTime.compareTo(end) <= 0
          case _                        => true

      case _ => false

trait Crawler:

  def crawl(interval: Crawler.Interval): Task[Crawler.Stream]
