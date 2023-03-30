package webapi.model

case class UserClaims(
    sub: Option[String] = None,
    email: Option[String] = None
)
