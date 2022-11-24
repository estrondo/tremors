ThisBuild / organization := "com.github.estrondo.tremors"
ThisBuild / scalaVersion := "3.2.1"
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
    core,
    testkitCore,
    cbor,
    webapi1x,
    graboidProtocol,
    testkitGraboidProtocol,
    graboid,
    farango,
    ziorango,
    quakeml,
    cborQuakeml,
    testkitQuakeml,
    testkitZIOTestcontainers,
    zioAppStarter
  )

lazy val core = (project in file("core"))
  .settings(
    name := "core"
  )

lazy val testkitCore = (project in file("testkit/core"))
  .settings(
    name := "teskit-core"
  )

lazy val cbor = (project in file("cbor/core"))
  .settings(
    name := "cbor-core",
    libraryDependencies ++= Seq(
      Dependencies.Borer
    ).flatten
  )

lazy val farango = (project in file("arangodb/farango"))
  .settings(
    name := "farango", // Functional ArangoDB
    libraryDependencies ++= Seq(
      Dependencies.ArangoDB
    ).flatten
  )

lazy val ziorango = (project in file("arangodb/ziorango"))
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

lazy val testkitQuakeml = (project in file("testkit/quakeml"))
  .settings(
    name := "testkit-quakeml"
  )
  .dependsOn(
    quakeml
  )

lazy val cborQuakeml = (project in file("cbor/quakeml"))
  .settings(
    name := "cbor-quakeml",
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
    cbor
  )

lazy val testkitGraboidProtocol = (project in file("testkit/graboid-protocol"))
  .settings(
    name := "testkit-graboid-protocol"
  )
  .dependsOn(
    testkitCore,
    graboidProtocol
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
  .enablePlugins(DockerPlugin, DockerHelperPlugin)
  .settings(
    dockerBaseImage    := "eclipse-temurin:17-jdk-alpine",
    dockerRepository   := Some("docker.estrondo.io"),
    dockerUpdateLatest := true
  )
  .dependsOn(
    core,
    graboidProtocol,
    zioAppStarter,
    farango,
    ziorango,
    quakeml,
    cborQuakeml,
    testkitQuakeml           % Test,
    testkitZIOTestcontainers % Test,
    testkitGraboidProtocol   % Test
  )

lazy val testkitZIOTestcontainers = (project in file("testkit/zio-testcontainers"))
  .settings(
    name := "testkit-zio-testcontainers",
    libraryDependencies ++= Seq(
      Dependencies.ZIO,
      Dependencies.ZIOKafka,
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
      Dependencies.Mockito,
      Dependencies.Logging
    ).flatten,
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
  )
  .enablePlugins(BuildInfoPlugin)
  .settings(
    buildInfoPackage := "webapi1x"
  )
  .dependsOn(
    core,
    zioAppStarter,
    graboidProtocol,
    testkitGraboidProtocol % Test,
    testkitZIOTestcontainers
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
