package webapi.config

import java.time.Duration

case class OpenIDProviderConfig(
    discoveryEndpoint: Option[String],
    jwksEndpoint: Option[String],
    jwksTTL: Duration
)
