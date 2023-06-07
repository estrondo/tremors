import sbt.*

//noinspection TypeAnnotation
object Dependencies {

  val ZIO = Seq(
    "dev.zio" %% "zio-streams" % Version.ZIO,
    "dev.zio" %% "zio"         % Version.ZIO
  )

  val ZIOConfig = Seq(
    "dev.zio" %% "zio-config"          % Version.ZIOConfig,
    "dev.zio" %% "zio-config-typesafe" % Version.ZIOConfig,
    "dev.zio" %% "zio-config-magnolia" % Version.ZIOConfig
  )

  val ZIOLogging = Seq(
    "dev.zio"  %% "zio-logging"        % Version.ZIOLogging,
    "dev.zio"  %% "zio-logging-slf4j2" % Version.ZIOLogging,
    "org.slf4j" % "slf4j-reload4j"     % "2.0.7"
  )

  val ZIOKafka = Seq(
    "dev.zio" %% "zio-kafka"         % Version.ZIOKafka,
    "dev.zio" %% "zio-kafka-testkit" % Version.ZIOKafka % Test
  )

  object Version {
    val ZIO        = "2.0.15"
    val ZIOConfig  = "4.0.0-RC16"
    val ZIOLogging = "2.1.13"
    val ZIOKafka   = "2.3.1"
  }
}
