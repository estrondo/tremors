import sbt._

object Dependencies {

  val ZHttpVersion            = "2.0.0-RC11"
  val ZKafkaVersion           = "2.0.7"
  val ZIOVersion              = "2.0.8"
  val ZIOLoggingVersion       = "2.1.9"
  val ZIOConfigVersion        = "3.0.7"
  val ZIOMockVersion          = "1.0.0-RC8"
  val ZIOKafkaVersion         = "2.0.7"
  val MUnitVersion            = "1.0.0-M6"
  val MUnitZIOVersion         = "0.1.0"
  val LemonScalaUriVersion    = "4.0.3"
  val Log4j2Version           = "2.19.0"
  val SLF4jVersion            = "2.0.3"
  val AaltoXmlVersion         = "1.3.2"
  val MockitoVersion          = "4.8.0"
  val ArangoDBVersion         = "6.20.0"
  val ArangoVelocypackVersion = "3.0.1"
  val BorerVersion            = "1.10.2"
  val TestcontainersVersion   = "0.40.12"
  val MacwireVersion          = "2.5.8"
  val ZIOJsonVersion          = "0.4.2"
  val DucktapeVersion         = "0.1.3"
  val SweetMockitoVersion     = "1.0.0+3-88ef51e9"
  val ScalaCommonVersion      = "2.3.4"
  val FarangoVersion          = "0.0.1-SNAPSHOT"
  val JacksonVersion          = "2.14.2"

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
    "dev.zio" %% "zio-logging"       % ZIOLoggingVersion,
    "dev.zio" %% "zio-logging-slf4j" % ZIOLoggingVersion
  )

  val ZIOConfig = Seq(
    "dev.zio" %% "zio-config"          % ZIOConfigVersion,
    "dev.zio" %% "zio-config-magnolia" % ZIOConfigVersion,
    "dev.zio" %% "zio-config-typesafe" % ZIOConfigVersion
  )

  val ZIOKafka = Seq(
    "dev.zio" %% "zio-kafka" % ZIOKafkaVersion
  )

  val LemonScalaUri = Seq(
    "io.lemonlabs" %% "scala-uri" % LemonScalaUriVersion // exclude ("org.typelevel", "cats-core_3") exclude("org.typelevel", "cats-parse_3")
  )

  val Testcontainers = Seq(
    "com.dimafeng" %% "testcontainers-scala-core"  % TestcontainersVersion % Test,
    "com.dimafeng" %% "testcontainers-scala-kafka" % TestcontainersVersion % Test
  )

  val Logging = Seq(
    "org.apache.logging.log4j" % "log4j-slf4j-impl" % Log4j2Version
  )

  val AaltoXml = Seq(
    "com.fasterxml" % "aalto-xml" % AaltoXmlVersion
  )

  val SweetMockito = Seq(
    "org.mockito"   % "mockito-core"      % MockitoVersion      % Test,
    "one.estrondo" %% "sweet-mockito"     % SweetMockitoVersion % Test,
    "one.estrondo" %% "sweet-mockito-zio" % SweetMockitoVersion % Test
  )

  val ArangoDB = Seq(
    "com.arangodb"                  % "arangodb-java-driver"          % ArangoDBVersion,
    "com.arangodb"                  % "jackson-dataformat-velocypack" % ArangoVelocypackVersion,
    "com.fasterxml.jackson.module" %% "jackson-module-scala"          % JacksonVersion
  )

  val Borer = Seq(
    "io.bullet" %% "borer-core"       % BorerVersion,
    "io.bullet" %% "borer-derivation" % BorerVersion
  )

  val Macwire = Seq(
    "com.softwaremill.macwire" %% "macros"  % MacwireVersion % Provided,
    "com.softwaremill.common"  %% "tagging" % ScalaCommonVersion
  )

  val ZIOJson = Seq(
    "dev.zio" %% "zio-json" % ZIOJsonVersion
  )

  val Ducktape = Seq(
    "io.github.arainko" %% "ducktape" % DucktapeVersion
  )

  val Jackson = Seq(
    "com.fasterxml.jackson.core" % "jackson-databind" % JacksonVersion
  )

  val Farango = Seq(
    "one.estrondo" %% "farango" % FarangoVersion,
    "one.estrondo" %% "zarango" % FarangoVersion
  )

  val GRPC = Seq(
    "io.grpc"               % "grpc-netty"           % "1.41.0",
    "com.thesamet.scalapb" %% "scalapb-runtime-grpc" % scalapb.compiler.Version.scalapbVersion
  )

  val ZCache = Seq(
    "dev.zio" %% "zio-cache" % "0.1.5"
  )
}
