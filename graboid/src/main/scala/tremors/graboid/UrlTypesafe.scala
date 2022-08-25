package tremors.graboid

import io.lemonlabs.uri.typesafe.QueryValue

import java.time.ZonedDateTime

object UrlTypesafe:

  // noinspection ConvertExpressionToSAM
  given QueryValue[ZonedDateTime] = new QueryValue[ZonedDateTime]:
    override def queryValue(a: ZonedDateTime): Option[String] = Some(a.toString)
