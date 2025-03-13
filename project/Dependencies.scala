import sbt.*

//noinspection TypeAnnotation
object Dependencies {

  val SweetMockito = Seq(
    "one.estrondo" %% "sweet-mockito-zio" % Version.SweetMockito % Test,
  )

  val AaltoXML = Seq(
    "com.fasterxml" % "aalto-xml" % Version.AaltoXML,
  )

  val Borer = Seq(
    "io.bullet" %% "borer-core"       % "1.15.0",
    "io.bullet" %% "borer-derivation" % "1.15.0",
  )

  val FarangoDucktape = Seq(
    "one.estrondo" %% "farango-ducktape" % Version.Farango exclude ("org.slf4j", "slf4j-api"),
  )

  val ZIOFarango = Seq(
    "one.estrondo"                 %% "farango-zio"          % Version.Farango exclude ("org.slf4j", "slf4j-api"),
    "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.18.2",
  )

  val ZIO = Seq(
    "dev.zio" %% "zio"          % Version.ZIO,
    "dev.zio" %% "zio-test"     % Version.ZIO % Test,
    "dev.zio" %% "zio-test-sbt" % Version.ZIO % Test,
  )

  val ZIOConfig = Seq(
    "dev.zio" %% "zio-config"          % Version.ZIOConfig,
    "dev.zio" %% "zio-config-typesafe" % Version.ZIOConfig,
    "dev.zio" %% "zio-config-magnolia" % Version.ZIOConfig,
  )

  val ZIOLogging = Seq(
    "dev.zio"                 %% "zio-logging"        % Version.ZIOLogging,
    "dev.zio"                 %% "zio-logging-slf4j2" % Version.ZIOLogging,
    "org.apache.logging.log4j" % "log4j-slf4j2-impl"  % "2.24.3",
  )

  val ZIOHttp = Seq(
    "dev.zio" %% "zio-http" % Version.ZIOHttp,
  )

  val ZIOKafka = Seq(
    "dev.zio"  %% "zio-kafka"         % Version.ZIOKafka,
    ("dev.zio" %% "zio-kafka-testkit" % Version.ZIOKafka)
      .exclude("com.fasterxml.jackson.module", "jackson-module-scala_2.13"),
  )

  val ZIOStream = Seq(
    "dev.zio" %% "zio-streams" % Version.ZIO,
  )

  val TestcontainersScala = Seq(
    "com.dimafeng" %% "testcontainers-scala-core" % Version.TestcontainersScala,
  )

  val TestcontainersKafka = Seq(
    "com.dimafeng" %% "testcontainers-scala-kafka" % Version.TestcontainersScala,
  )

  val JWT = Seq(
    "com.github.jwt-scala" %% "jwt-core" % Version.JWT,
  )

  val gRPCTesting = Seq(
    "io.grpc" % "grpc-testing" % "1.70.0" % Test,
  )

  val JTS = Seq(
    "org.locationtech.jts"     % "jts-core"             % "1.20.0",
    "com.graphhopper.external" % "jackson-datatype-jts" % "2.14",
  )

  val Moidc4sZIO = Seq(
    "one.estrondo" %% "moidc4s-zio-http"      % Version.Moidc4s,
    "one.estrondo" %% "moidc4s-jwt-scala-zio" % Version.Moidc4s,
    "one.estrondo" %% "moidc4s-zio-json"      % Version.Moidc4s,
  )

  val Wiremock = Seq(
    "org.wiremock" % "wiremock" % "3.12.0" % Test,
  )

  val Macwire = Seq(
    "com.softwaremill.macwire" %% "macros" % Version.Macwire % "provided",
  )
}

object Version {
  val AaltoXML            = "1.3.3"
  val Farango             = "0.2.1"
  val SweetMockito        = "1.2.0+1-32360397+20230706-1831"
  val TestcontainersScala = "0.41.8"
  val ZIO                 = "2.1.15"
  val ZIOConfig           = "4.0.3"
  val ZIOLogging          = "2.5.0"
  val ZIOKafka            = "2.11.0"
  val ZIOHttp             = "3.0.1"
  val JWT                 = "10.0.4"
  val Moidc4s             = "0.1.0"
  val ZioGrpc             = "0.6.2"
  val Macwire             = "2.6.6"
}
