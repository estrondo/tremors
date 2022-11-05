package webapi1x

import zioapp.ZProfile
import zio.ExitCode
import zio.Scope
import zio.ZIO
import zio.ZIOAppArgs
import zio.ZIOAppDefault

object WebApi extends ZIOAppDefault:

  case class Root(webapi: WebApiConfig)

  case class HttpConfig(
      hostname: Option[String],
      port: Option[Int],
      threads: Option[Int]
  )

  case class WebApiConfig(
      http: HttpConfig,
      kafka: KafkaConfig
  )

  case class KafkaConfig(
      producer: KafkaProducerConfig
  )

  case class KafkaProducerConfig(
      bootstrapServers: List[String]
  )

  override def run: ZIO[ZIOAppArgs & Scope, Any, ExitCode] =
    for
      root          <- ZProfile.loadOnlyConfig[Root]()
      kafkaModule   <- KafkaModule(root.webapi)
      crawlerModule <- CrawlerModule(root.webapi, kafkaModule)
      httpModule    <- HttpModule(root.webapi)
      httpApp       <- RouterModule(crawlerModule).flatMap(_.createApp())
      _             <- httpModule.runServer(httpApp)
    yield ExitCode.success
