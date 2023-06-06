ThisBuild / organization := "one.estrondo"
ThisBuild / scalaVersion := "3.3.0"
ThisBuild / version ~= (_.replace('+', '-'))
ThisBuild / dynver ~= (_.replace('+', '-'))
ThisBuild / resolvers ++= Resolver.sonatypeOssRepos("snapshots")
ThisBuild / Test / fork := true


lazy val root = (project in file("."))
  .settings(
    name := "tremors"
  )