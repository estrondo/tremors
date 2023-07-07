ThisBuild / organization := "one.estrondo"
ThisBuild / scalaVersion := "3.3.0"
ThisBuild / version ~= (_.replace('+', '-'))
ThisBuild / dynver ~= (_.replace('+', '-'))
ThisBuild / resolvers ++= Resolver.sonatypeOssRepos("snapshots")
ThisBuild / Test / fork  := true

ThisBuild / scalacOptions ++= Seq(
  "-Wunused:all",
  "-explain",
  "-deprecation",
  "-unchecked"
)

lazy val root = (project in file("."))
  .settings(
    name := "tremors"
  )
  .aggregate(
    graboid,
    toph,
    zioStarter
  )

lazy val generator = (project in file("generator"))
  .settings(
    name := "tremors-generator"
  )

lazy val graboid = (project in file("graboid"))
  .settings(
    name := "tremors-graboid",
    libraryDependencies ++= Seq(
      Dependencies.ZIO,
      Dependencies.ZIOStream,
      Dependencies.AaltoXML,
      Dependencies.Macwire,
      Dependencies.SweetMockito
    ).flatten,
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
  )
  .dependsOn(
    zioStarter,
    zioKafka,
    zioFarango,
    generator,
    generator % "test->test"
  )
  .enablePlugins(JavaAppPackaging, DockerPlugin)

lazy val toph = (project in file("toph"))
  .settings(
    name := "tremors-toph",
    libraryDependencies ++= Seq(
      Dependencies.ZIO,
      Dependencies.ZIOStream
    ).flatten
  )
  .dependsOn(
    zioStarter,
    zioKafka,
    zioFarango
  )
  .enablePlugins(JavaAppPackaging, DockerPlugin)

lazy val zioStarter = (project in file("zio/starter"))
  .settings(
    name := "tremors-zio-starter",
    libraryDependencies ++= Seq(
      Dependencies.ZIO,
      Dependencies.ZIOConfig,
      Dependencies.ZIOLogging
    ).flatten
  )

lazy val zioKafka = (project in file("zio/kafka"))
  .settings(
    name := "tremors-zio-kafka",
    libraryDependencies ++= Seq(
      Dependencies.ZIO,
      Dependencies.ZIOKafka
    ).flatten
  )

lazy val zioFarango = (project in file("zio/farango"))
  .settings(
    name := "tremors-zio-farango",
    libraryDependencies ++= Seq(
      Dependencies.ZIO,
      Dependencies.ZIOFarango
    ).flatten
  )
