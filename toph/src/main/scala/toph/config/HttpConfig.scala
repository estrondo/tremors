package toph.config

case class HttpConfig(
    client: HttpConfig.Client,
)

object HttpConfig:
  case class Client()
