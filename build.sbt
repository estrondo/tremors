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
    `farango-query`,
    `farango-zio-starter`,
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
    `testkit-zio-repository`,
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
      Dependencies.SweetMockito.map(_.withConfigurations(None))
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
      Dependencies.Ducktape,
      Dependencies.Jackson
    ).flatten
  )

lazy val `farango-query` = (project in file("arangodb/query"))
  .settings(
    name := "farango-query",
    libraryDependencies ++= Seq(
      Dependencies.Farango
    ).flatten
  )

lazy val `farango-zio-starter` = (project in file("arangodb/farango-zio-starter"))
  .settings(
    name := "farango-zio-starter",
    libraryDependencies ++= Seq(
      Dependencies.ArangoDB,
      Dependencies.Farango,
      Dependencies.ZIO,
      Dependencies.ZIOConfig
    ).flatten
  )
  .dependsOn(
    `farango-data`
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

lazy val `toph-message-protocol` = (project in file("toph-message-protocol"))
  .settings(
    name := "toph-message-protocol"
  )
  .dependsOn(
    cbor
  )

lazy val toph = (project in file("toph"))
  .settings(
    name                 := "toph",
    libraryDependencies ++= Seq(
      Dependencies.ZIO,
      Dependencies.ZHttp,
      Dependencies.ZIOLogging,
      Dependencies.Logging,
      Dependencies.ZIOConfig,
      Dependencies.ZIOKafka,
      Dependencies.Farango,
      Dependencies.Ducktape,
      Dependencies.Macwire,
      Dependencies.GRPC,
      Dependencies.ZCache,
      Dependencies.JTS,
      Dependencies.JTSJackson
    ).flatten,
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework"),
    Compile / PB.targets := Seq(
      scalapb.gen(grpc = true)          -> (Compile / sourceManaged).value / "scalapb",
      scalapb.zio_grpc.ZioCodeGenerator -> (Compile / sourceManaged).value / "scalapb"
    )
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
    `farango-zio-starter`,
    `farango-data`,
    `farango-query`,
    zkafka,
    `toph-message-protocol`,
    `testkit-quakeml`            % Test,
    `testkit-zio-testcontainers` % Test,
    `testkit-zio-repository`     % Test
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
      Dependencies.SweetMockito,
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
    `farango-zio-starter`,
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
      Dependencies.Testcontainers.map(_.withConfigurations(Some(Compile.name))),
      Dependencies.Farango,
      Dependencies.ArangoDB,
      Dependencies.JTSJackson
    ).flatten
  )
  .dependsOn(
    `testkit-core`,
    `farango-data`
  )

lazy val `testkit-zio-repository` = (project in file("testkit/zio-repository"))
  .settings(
    name := "testkit-zio-repository",
    libraryDependencies ++= Seq(
      Dependencies.ZIO.map(_.withConfigurations(Some(Compile.name)))
    ).flatten
  )
  .dependsOn(
    `testkit-core`,
    `testkit-zio-testcontainers`
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
      Dependencies.SweetMockito,
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
      Dependencies.SweetMockito,
      Dependencies.ZIOLogging,
      Dependencies.Logging,
      Dependencies.ZIOConfig
    ).flatten
  )
  .enablePlugins(ITPlugin)
  .dependsOn(
    `testkit-core`,
    `testkit-zio-testcontainers`
  )
