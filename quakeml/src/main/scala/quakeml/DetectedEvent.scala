package quakeml

import java.time.ZonedDateTime

case class DetectedEvent(
    collected: ZonedDateTime,
    event: Event
):

  def id: String = event.publicID.uri
