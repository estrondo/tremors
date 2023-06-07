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

lazy val graboid = (project in file("graboid"))
  .settings(
    name := "tremors-graboid",
    libraryDependencies ++= Seq(
      Dependencies.ZIO
    ).flatten
  )
  .dependsOn(
    zioStarter,
    zioKafka
  )

lazy val toph = (project in file("toph"))
  .settings(
    name := "tremors-toph"
  )
  .dependsOn(
    zioStarter,
    zioKafka
  )

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
