
ThisBuild / organization := "com.github.estrondo.tremors"
ThisBuild / version := "1.0.0"
ThisBuild / scalaVersion := "3.1.3"

lazy val root = (project in file("."))
  .settings(
    name := "tremors"
  )
  .aggregate(
    `webapp1`,
    `webapp-core`,
    `graboid`
  )

lazy val `graboid` = (project in file("graboid"))
  .settings(
    name := "graboid",
    libraryDependencies ++= Seq(
      Dependencies.ZIO,
      Dependencies.ZIOLogging,
      Dependencies.ZIOConfig
    ).flatten
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
