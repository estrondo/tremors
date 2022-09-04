import sbt._

object Dependencies {

  val ZHttpVersion         = "2.0.0-RC10"
  val ZKafkaVersion        = "2.0.0-M3"
  val ZIOVersion           = "2.0.0"
  val ZIOLoggingVersion    = "2.1.0"
  val ZIOConfigVersion     = "3.0.2"
  val ZIOMockVersion       = "1.0.0-RC8"
  val MUnitVersion         = "1.0.0-M6"
  val MUnitZIOVersion      = "0.1.0"
  val LemonScalaUriVersion = "4.0.2"

  val ZHttp = Seq(
    "io.d11" %% "zhttp" % ZHttpVersion
  )

  val ZKafka = Seq(
    "dev.zio" %% "zio-kafka" % ZKafkaVersion
  )

  val ZIO = Seq(
    "dev.zio" %% "zio"               % ZIOVersion,
    "dev.zio" %% "zio-streams"       % ZIOVersion,
    "dev.zio" %% "zio-test"          % ZIOVersion % Test,
    "dev.zio" %% "zio-test-sbt"      % ZIOVersion % Test,
    "dev.zio" %% "zio-test-magnolia" % ZIOVersion % Test
  )

  val MUnitZio = Seq(
    "org.scalameta"      %% "munit"     % MUnitVersion    % Test,
    "com.github.poslegm" %% "munit-zio" % MUnitZIOVersion % Test
  )

  val ZIOLogging = Seq(
    "dev.zio"       %% "zio-logging"       % ZIOLoggingVersion,
    "dev.zio"       %% "zio-logging-slf4j" % ZIOLoggingVersion,
    "ch.qos.logback" % "logback-classic"   % "1.3.0-beta0"
  )

  val ZIOConfig = Seq(
    "dev.zio" %% "zio-config"          % ZIOConfigVersion,
    "dev.zio" %% "zio-config-magnolia" % ZIOConfigVersion,
    "dev.zio" %% "zio-config-typesafe" % ZIOConfigVersion
  )

  val LemonScalaUri = Seq(
    "io.lemonlabs" %% "scala-uri" % LemonScalaUriVersion
  )

  val Testcontainers = Seq(
    "com.dimafeng" %% "testcontainers-scala-core" % "0.40.10" % Test
  )

  val Logack = Seq(
    "ch.qos.logback" % "logback-classic" % "1.4.0"
  )
}
