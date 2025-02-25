package toph.config

case class SecurityConfig(
    secrets: Seq[SecretConfig],
    tokenExpiration: Int,
    openIdProvider: List[OpenIdProviderConfig],
)

case class OpenIdProviderConfig(
    id: String,
    discoveryEndpoint: Option[String],
)

case class SecretConfig(
    secret: String,
    algorithm: String,
)
