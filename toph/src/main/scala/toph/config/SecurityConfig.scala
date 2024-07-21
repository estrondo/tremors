package toph.config

case class SecurityConfig(
    secret: String,
    algorithm: String,
    tokenExpiration: Int,
    openIdProvider: List[OpenIdProviderConfig],
)

case class OpenIdProviderConfig(
    id: String,
    discoveryEndpoint: Option[String],
)
