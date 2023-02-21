package toph.model.data

import java.time.ZonedDateTime

case class CreationInfoData(
    agencyID: Option[String],
    agencyURI: Option[String],
    author: Option[String],
    creationTime: Option[ZonedDateTime],
    version: Option[String]
)
