package tremors.graboid

import io.lemonlabs.uri.typesafe.QueryValue

import java.time.ZonedDateTime

object UrlTypesafe:

  given QueryValue[ZonedDateTime] = zonedDateTime => Some(zonedDateTime.toString())
