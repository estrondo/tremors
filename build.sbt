ThisBuild / organization := "com.github.estrondo.tremors"
ThisBuild / scalaVersion := "3.2.0"
ThisBuild / isSnapshot   := true
ThisBuild / Test / fork  := true
ThisBuild / version ~= (_.replace('+', '-'))
ThisBuild / dynver ~= (_.replace('+', '-'))
ThisBuild / resolvers += Resolver.sonatypeRepo("snapshots")

Test / run / javaOptions += "-Dtremors.profile=test"

ThisBuild / scalacOptions ++= Seq(
  "--explain",
  "-feature",
  "-language:implicitConversions"
)

lazy val root = (project in file("."))
  .settings(
    name := "tremors"
  )
  .aggregate(
    borerCodec,
    webapi1x,
    graboidProtocol,
    graboid,
    farango,
    ziorango,
    quakeml,
    quakemlCBOR,
    quakemlTestKit,
    zioTestcontainers,
    zioAppStarter
  )

lazy val borerCodec = (project in file("borer-codec"))
  .settings(
    name := "borer-codec",
    libraryDependencies ++= Seq(
      Dependencies.Borer
    ).flatten
  )

lazy val farango = (project in file("farango"))
  .settings(
    name := "farango", // Functional ArangoDB
    libraryDependencies ++= Seq(
      Dependencies.ArangoDB
    ).flatten
  )

lazy val ziorango = (project in file("ziorango"))
  .settings(
    name := "ziorango",
    libraryDependencies ++= Seq(
      Dependencies.ZIO
    ).flatten
  )
  .dependsOn(
    farango
  )

lazy val quakeml = (project in file("quakeml"))
  .settings(
    name := "quakeml",
    libraryDependencies ++= Seq(
    ).flatten
  )

lazy val quakemlTestKit = (project in file("quakeml-testkit"))
  .settings(
    name := "quakeml-testkit"
  )
  .dependsOn(
    quakeml
  )

lazy val quakemlCBOR = (project in file("quakeml-cbor"))
  .settings(
    name := "quakeml-cbor",
    libraryDependencies ++= Seq(
      Dependencies.Borer
    ).flatten
  )
  .dependsOn(quakeml)

lazy val graboidProtocol = (project in file("graboid-protocol"))
  .settings(
    name := "graboid-protocol",
    libraryDependencies ++= Seq(
      Dependencies.Borer
    ).flatten
  )
  .dependsOn(
    borerCodec
  )

lazy val graboid = (project in file("graboid"))
  .settings(
    name := "graboid",
    libraryDependencies ++= Seq(
      Dependencies.ZIO,
      Dependencies.ZHttp,
      Dependencies.ZIOLogging,
      Dependencies.Logging,
      Dependencies.ZIOConfig,
      Dependencies.ZIOKafka,
      Dependencies.LemonScalaUri,
      Dependencies.AaltoXml,
      Dependencies.Mockito,
      Dependencies.ArangoDB,
      Dependencies.Macwire
    ).flatten,
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
  )
  .enablePlugins(BuildInfoPlugin)
  .settings(
    buildInfoPackage := "graboid"
  )
  .enablePlugins(AshScriptPlugin)
  .enablePlugins(GraalVMNativeImagePlugin)
  .enablePlugins(DockerPlugin)
  .settings(
    dockerBaseImage    := "eclipse-temurin:17-jdk-alpine",
    dockerUpdateLatest := true
  )
  .dependsOn(
    graboidProtocol,
    zioAppStarter,
    farango,
    ziorango,
    quakeml,
    quakemlCBOR,
    quakemlTestKit    % Test,
    zioTestcontainers % Test
  )

lazy val zioTestcontainers = (project in file("zio-testcontainers"))
  .settings(
    name := "zio-testcontainers",
    libraryDependencies ++= Seq(
      Dependencies.ZIO,
      Dependencies.Testcontainers.map(_.withConfigurations(Some(Compile.name)))
    ).flatten
  )

lazy val webapi1x = (project in file("webapi1x"))
  .settings(
    name := "webapp1x",
    libraryDependencies ++= Seq(
      Dependencies.ZIO,
      Dependencies.ZHttp,
      Dependencies.Macwire,
      Dependencies.ZKafka,
      Dependencies.Ducktape,
      Dependencies.Mockito
    ).flatten
  )
  .enablePlugins(BuildInfoPlugin)
  .settings(
    buildInfoPackage := "webapi1x"
  )
  .dependsOn(
    zioAppStarter,
    graboidProtocol
  )

lazy val zioAppStarter = (project in file("zio-app-starter"))
  .settings(
    name := "zio-app-starter",
    libraryDependencies ++= Seq(
      Dependencies.ZIO,
      Dependencies.ZIOConfig,
      Dependencies.ZIOJson
    ).flatten
  )
