package quakeml

import java.time.ZonedDateTime

case class QuakeMLCreationInfo(
    agencyID: Option[String],
    agencyURI: Option[QuakeMLResourceReference],
    author: Option[String],
    authorURI: Option[QuakeMLResourceReference],
    creationTime: Option[ZonedDateTime],
    version: Option[String]
)
