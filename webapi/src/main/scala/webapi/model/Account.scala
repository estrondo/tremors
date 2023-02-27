package webapi.model

import java.time.ZonedDateTime

object Account:

  case class Update(
      name: String
  )

case class Account(
    email: String,
    name: String,
    active: Boolean,
    secret: String,
    createdAt: ZonedDateTime
)
