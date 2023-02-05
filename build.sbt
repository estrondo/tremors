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
    `testkit-core`,
    `farango-data`,
    cbor,
    webapi1x,
    `graboid-protocol`,
    `testkit-graboid-protocol`,
    graboid,
    toph,
    quakeml,
    `cbor-quakeml`,
    `testkit-quakeml`,
    `testkit-zio-testcontainers`,
    `zip-app-starter`,
    zkafka
  )

lazy val core = (project in file("core"))
  .settings(
    name := "core"
  )

lazy val `testkit-core` = (project in file("testkit/core"))
  .settings(
    name := "teskit-core",
    libraryDependencies ++= Seq(
      Dependencies.Mockito.map(_.withConfigurations(None))
    ).flatten
  )

lazy val cbor = (project in file("cbor/core"))
  .settings(
    name := "cbor-core",
    libraryDependencies ++= Seq(
      Dependencies.Borer
    ).flatten
  )

lazy val `farango-data` = (project in file("arangodb/data"))
  .settings(
    name := "farango-data",
    libraryDependencies ++= Seq(
      Dependencies.Ducktape
    ).flatten
  )

lazy val quakeml = (project in file("quakeml"))
  .settings(
    name := "quakeml",
    libraryDependencies ++= Seq(
    ).flatten
  )

lazy val `testkit-quakeml` = (project in file("testkit/quakeml"))
  .settings(
    name := "testkit-quakeml"
  )
  .dependsOn(
    `testkit-core`,
    quakeml
  )

lazy val `cbor-quakeml` = (project in file("cbor/quakeml"))
  .settings(
    name := "cbor-quakeml",
    libraryDependencies ++= Seq(
      Dependencies.Borer
    ).flatten
  )
  .dependsOn(quakeml, cbor)

lazy val `graboid-protocol` = (project in file("graboid-protocol"))
  .settings(
    name := "graboid-protocol",
    libraryDependencies ++= Seq(
      Dependencies.Borer
    ).flatten
  )
  .dependsOn(
    cbor
  )

lazy val `testkit-graboid-protocol` = (project in file("testkit/graboid-protocol"))
  .settings(
    name := "testkit-graboid-protocol"
  )
  .dependsOn(
    `testkit-core`,
    `graboid-protocol`
  )

lazy val toph = (project in file("toph"))
  .settings(
    name := "toph",
    libraryDependencies ++= Seq(
      Dependencies.ZIO,
      Dependencies.ZHttp,
      Dependencies.ZIOLogging,
      Dependencies.Logging,
      Dependencies.ZIOConfig,
      Dependencies.ZIOKafka,
      Dependencies.Farango,
      Dependencies.Macwire
    ).flatten
  )
  .enablePlugins(ITPlugin)
  .enablePlugins(BuildInfoPlugin)
  .settings(
    buildInfoPackage := "toph"
  )
  .enablePlugins(AshScriptPlugin)
  .enablePlugins(DockerPlugin)
  .settings(
    dockerBaseImage      := "docker.io/eclipse-temurin:17-jdk-alpine",
    dockerRepository     := Some("docker.io/rthoth"),
    dockerUpdateLatest   := false,
    Docker / packageName := "estrondo",
    Docker / version ~= ("toph_" + _),
    dockerAliases ++= Seq(dockerAlias.value.withTag(Some("toph")))
  )
  .dependsOn(
    core,
    `zip-app-starter`,
    quakeml,
    `cbor-quakeml`,
    `testkit-quakeml`            % Test,
    `testkit-zio-testcontainers` % Test
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
      Dependencies.Macwire,
      Dependencies.Ducktape,
      Dependencies.Farango
    ).flatten,
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
  )
  .enablePlugins(ITPlugin)
  .enablePlugins(BuildInfoPlugin)
  .settings(
    buildInfoPackage := "graboid"
  )
  .enablePlugins(AshScriptPlugin)
  .enablePlugins(GraalVMNativeImagePlugin)
  .enablePlugins(DockerPlugin, DockerHelperPlugin)
  .settings(
    dockerBaseImage      := "docker.io/eclipse-temurin:17-jdk-alpine",
    dockerRepository     := Some("docker.io/rthoth"),
    dockerUpdateLatest   := false,
    Docker / packageName := "estrondo",
    Docker / version ~= ("graboid_" + _),
    dockerAliases ++= Seq(dockerAlias.value.withTag(Some("graboid")))
  )
  .dependsOn(
    core,
    `graboid-protocol`,
    `zip-app-starter`,
    `farango-data`,
    quakeml,
    `cbor-quakeml`,
    zkafka,
    `testkit-quakeml`            % Test,
    `testkit-zio-testcontainers` % Test,
    `testkit-graboid-protocol`   % Test
  )

lazy val `testkit-zio-testcontainers` = (project in file("testkit/zio-testcontainers"))
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
  .enablePlugins(ITPlugin)
  .enablePlugins(BuildInfoPlugin)
  .settings(
    buildInfoPackage := "webapi1x"
  )
  .dependsOn(
    core,
    `zip-app-starter`,
    `graboid-protocol`,
    `testkit-graboid-protocol` % Test,
    `testkit-zio-testcontainers`
  )

lazy val `zip-app-starter` = (project in file("zio-app-starter"))
  .settings(
    name := "zio-app-starter",
    libraryDependencies ++= Seq(
      Dependencies.ZIO,
      Dependencies.ZIOConfig,
      Dependencies.ZIOJson
    ).flatten
  )

lazy val `zkafka` = (project in file("kafka/zkafka"))
  .settings(
    name := "zkafka",
    libraryDependencies ++= Seq(
      Dependencies.ZIO,
      Dependencies.ZKafka,
      Dependencies.Borer,
      Dependencies.Macwire,
      Dependencies.Mockito,
      Dependencies.ZIOLogging,
      Dependencies.Logging
    ).flatten
  )
  .enablePlugins(ITPlugin)
  .dependsOn(
    `testkit-core`,
    `testkit-zio-testcontainers`
  )
