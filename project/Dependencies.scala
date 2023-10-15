import sbt.*

//noinspection TypeAnnotation
object Dependencies {

  val SweetMockito = Seq(
    "one.estrondo" %% "sweet-mockito-zio" % Version.SweetMockito % Test
  )

  val AaltoXML = Seq(
    "com.fasterxml" % "aalto-xml" % Version.AaltoXML
  )

  val Borer = Seq(
    "io.bullet" %% "borer-core"       % "1.11.0",
    "io.bullet" %% "borer-derivation" % "1.11.0"
  )

  val Macwire = Seq(
    "com.softwaremill.macwire" %% "macros" % Version.Macwire % Provided,
    "com.softwaremill.macwire" %% "util"   % Version.Macwire
  )

  val FarangoDucktape = Seq(
    "one.estrondo" %% "farango-ducktape" % Version.Farango exclude ("org.slf4j", "slf4j-api")
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

  val ZIOHttp = Seq(
    "dev.zio" %% "zio-http" % Version.ZIOHttp
  )

  val ZIOKafka = Seq(
    "dev.zio"  %% "zio-kafka"         % Version.ZIOKafka,
    ("dev.zio" %% "zio-kafka-testkit" % Version.ZIOKafka)
      .exclude("com.fasterxml.jackson.module", "jackson-module-scala_2.13")
  )

  val ZIOStream = Seq(
    "dev.zio" %% "zio-streams" % Version.ZIO
  )

  val TestcontainersScala = Seq(
    "com.dimafeng" %% "testcontainers-scala-core" % Version.TestcontainersScala
  )

  val TestcontainersKafka = Seq(
    "com.dimafeng" %% "testcontainers-scala-kafka" % Version.TestcontainersScala
  )

  object Version {
    val AaltoXML            = "1.3.2"
    val Macwire             = "2.5.9"
    val Farango             = "0.2.1"
    val Reload4j            = "2.0.7"
    val SweetMockito        = "1.2.0+1-904f0c3e"
    val TestcontainersScala = "0.41.0"
    val ZIO                 = "2.0.18"
    val ZIOConfig           = "4.0.0-RC16"
    val ZIOLogging          = "2.1.14"
    val ZIOKafka            = "2.5.0"
    val ZIOHttp             = "3.0.0-RC2+115-3a6525ce-SNAPSHOT"
  }
}
