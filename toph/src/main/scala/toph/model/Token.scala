package toph.model

import java.time.ZonedDateTime

case class Token(
    key: String,
    expiration: ZonedDateTime,
    accountKey: String,
    accountEmail: String,
    accessToken: Array[Byte],
    accessTokenHash: String,
    accessTokenExpiration: ZonedDateTime,
    device: String,
    origin: Option[String],
    createdAt: ZonedDateTime,
)
