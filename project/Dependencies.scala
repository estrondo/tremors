import sbt.*

//noinspection TypeAnnotation
object Dependencies {

  val AaltoXML = Seq(
    "com.fasterxml" % "aalto-xml" % Version.AaltoXML
  )

  val ZIO = Seq(
    "dev.zio" %% "zio" % Version.ZIO,
    "dev.zio" %% "zio-test" % Version.ZIO % Test,
    "dev.zio" %% "zio-test-sbt" % Version.ZIO % Test,
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

  val ZIOStream = Seq(
    "dev.zio" %% "zio-streams" % Version.ZIO
  )

  object Version {
    val AaltoXML   = "1.3.2"
    val ZIO        = "2.0.15"
    val ZIOConfig  = "4.0.0-RC16"
    val ZIOLogging = "2.1.13"
    val ZIOKafka   = "2.3.1"
  }
}
