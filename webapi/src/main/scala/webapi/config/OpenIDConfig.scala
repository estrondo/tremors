package webapi.config

case class OpenIDConfig(
    providers: Map[String, OpenIDProviderConfig]
)
