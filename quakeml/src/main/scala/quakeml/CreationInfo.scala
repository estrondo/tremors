package quakeml

import java.time.ZonedDateTime

case class CreationInfo(
    agencyID: Option[String],
    agencyURI: Option[ResourceReference],
    author: Option[String],
    authorURI: Option[ResourceReference],
    creationTime: Option[ZonedDateTime],
    version: Option[String]
)
