ThisBuild / organization := "com.github.estrondo.tremors"
ThisBuild / scalaVersion := "3.2.0"
ThisBuild / isSnapshot   := true
ThisBuild / Test / fork  := true
ThisBuild / version ~= (_.replace('+', '-'))
ThisBuild / dynver ~= (_.replace('+', '-'))

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
    `webapp1`,
    `webapp-core`,
    `graboid`,
    farango,
    ziorango,
    quakeml,
    quakemlCBOR,
    quakemlTestKit
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

lazy val `graboid` = (project in file("graboid"))
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
      Dependencies.Testcontainers,
      Dependencies.AaltoXml,
      Dependencies.Mockito,
      Dependencies.ArangoDB
    ).flatten,
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
  )
  .enablePlugins(BuildInfoPlugin)
  .settings(
    buildInfoPackage := "tremors.graboid"
  )
  .enablePlugins(AshScriptPlugin)
  .enablePlugins(GraalVMNativeImagePlugin)
  .enablePlugins(DockerPlugin)
  .settings(
    dockerBaseImage    := "eclipse-temurin:17-jdk-alpine",
    dockerUpdateLatest := true
  )
  .dependsOn(
    farango,
    ziorango,
    quakeml,
    quakemlCBOR,
    quakemlTestKit % Test
  )

lazy val `webapp-core` = (project in file("webapp-core"))
  .settings(
    name := "webapp-core",
    libraryDependencies ++=
      Dependencies.ZKafka
  )

lazy val `webapp1` = (project in file("webapp1"))
  .settings(
    name := "webapp1",
    libraryDependencies ++=
      Dependencies.ZHttp
  )
  .dependsOn(`webapp-core`)
