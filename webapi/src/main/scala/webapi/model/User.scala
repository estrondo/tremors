package webapi.model

import java.time.ZonedDateTime

object User:

  case class Update(
      name: String
  )

case class User(
    email: String,
    name: String,
    createdAt: ZonedDateTime
)
