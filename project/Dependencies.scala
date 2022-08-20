import sbt._

object Dependencies {

  private val ZHttpVersion = "1.0.0.0-RC27"
  private val ZKafkaVersion = "2.0.0-M3"
  private val ZIOVersion = "2.0.0"
  private val ZIOLoggingVersion = "2.1.0"
  private val ZIOConfigVersion = "3.0.2"

  val ZHttp = Seq(
    "io.d11" %% "zhttp" % ZHttpVersion,
    "io.d11" %% "zhttp-test" % ZHttpVersion % Test
  )

  val ZKafka = Seq(
    "dev.zio" %% "zio-kafka" % ZKafkaVersion
  )

  val ZIO = Seq(
    "dev.zio" %% "zio" % ZIOVersion,
    "dev.zio" %% "zio-streams" % ZIOVersion
  )

  val ZIOLogging = Seq(
    "dev.zio" %% "zio-logging" % ZIOLoggingVersion,
    "dev.zio" %% "zio-logging-slf4j" % ZIOLoggingVersion,
    "ch.qos.logback" % "logback-classic" % "1.3.0-beta0"
  )

  val ZIOConfig = Seq(
    "dev.zio" %% "zio-config" % ZIOConfigVersion,
    "dev.zio" %% "zio-config-magnolia" % ZIOConfigVersion,
    "dev.zio" %% "zio-config-typesafe" % ZIOConfigVersion
  )
}
