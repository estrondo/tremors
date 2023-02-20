package webapi.model

import org.locationtech.jts.geom.MultiPolygon

object Alert:
  case class Update(
      area: Option[MultiPolygon],
      areaRadius: Option[Int],
      magnitudeFilter: Seq[MagnitudeFilter],
      location: Seq[Location]
  )

case class Alert(
    key: String,
    email: String,
    enabled: Boolean,
    area: Option[MultiPolygon],
    areaRadius: Option[Int],
    magnitudeFilter: Seq[MagnitudeFilter],
    location: Seq[Location]
)
