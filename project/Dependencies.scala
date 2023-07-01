import sbt.*

//noinspection TypeAnnotation
object Dependencies {

  val AaltoXML = Seq(
    "com.fasterxml" % "aalto-xml" % Version.AaltoXML
  )

  val Macwire = Seq(
    "com.softwaremill.macwire" %% "macros" % Version.Macwire % "provided",
    "com.softwaremill.macwire" %% "util"   % Version.Macwire
  )

  val ZIOFarango = Seq(
    "one.estrondo" %% "farango-zio" % Version.Farango exclude ("org.slf4j", "slf4j-api")
  )

  val ZIO = Seq(
    "dev.zio" %% "zio"          % Version.ZIO,
    "dev.zio" %% "zio-test"     % Version.ZIO % Test,
    "dev.zio" %% "zio-test-sbt" % Version.ZIO % Test
  )

  val ZIOConfig = Seq(
    "dev.zio" %% "zio-config"          % Version.ZIOConfig,
    "dev.zio" %% "zio-config-typesafe" % Version.ZIOConfig,
    "dev.zio" %% "zio-config-magnolia" % Version.ZIOConfig
  )

  val ZIOLogging = Seq(
    "dev.zio"                 %% "zio-logging"       % Version.ZIOLogging,
    "dev.zio"                 %% "zio-logging-slf4j" % Version.ZIOLogging,
    "org.apache.logging.log4j" % "log4j-slf4j-impl"  % "2.20.0"
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
    val Macwire    = "2.5.8"
    val Farango    = "0.0.1"
    val Reload4j   = "2.0.7"
    val ZIO        = "2.0.15"
    val ZIOConfig  = "4.0.0-RC16"
    val ZIOLogging = "2.1.13"
    val ZIOKafka   = "2.3.1"
  }
}
