package webapi.config

import java.time.Duration
import scala.concurrent.duration.FiniteDuration

case class OpenIDProviderConfig(
    discoveryEndpoint: Option[String],
    jwksEndpoint: Option[String],
    jwksTTL: Duration
)
