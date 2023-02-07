package toph.model

import java.time.ZonedDateTime

case class CreationInfo(
    agencyID: Option[String],
    agencyURI: Option[String],
    author: Option[String],
    creationTime: Option[ZonedDateTime],
    version: Option[String]
)
