package quakeml

import java.time.ZonedDateTime

case class QuakeMLDetectedEvent(
    collected: ZonedDateTime,
    event: QuakeMLEvent
):

  def id: String = event.publicID.uri
