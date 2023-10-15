ThisBuild / organization             := "one.estrondo"
ThisBuild / scalaVersion             := "3.3.1"
ThisBuild / version ~= (_.replace('+', '-'))
ThisBuild / dynver ~= (_.replace('+', '-'))
ThisBuild / resolvers ++= Resolver.sonatypeOssRepos("snapshots")
ThisBuild / Test / fork              := true
ThisBuild / Test / parallelExecution := false

ThisBuild / scalacOptions ++= Seq(
  "-Wunused:all",
//  "-explain",
  "-deprecation",
  "-unchecked"
)

lazy val root = (project in file("."))
  .settings(
    name := "tremors"
  )
  .aggregate(
    core,
    quakeML,
    graboid,
    toph,
    zioStarter,
    graboidIt,
    zioFarango,
    zioKafka,
    zioHttp
  )

lazy val core = (project in file("core"))
  .settings(
    name := "tremors-core"
  )

lazy val generator = (project in file("generator"))
  .settings(
    name := "tremors-generator"
  )

lazy val quakeML = (project in file("quakeml"))
  .settings(
    name := "tremors-quakeml",
    libraryDependencies ++= Seq(
      Dependencies.Borer
    ).flatten
  )
  .dependsOn(
    generator % Test,
    core      % Test,
    core      % "test->test"
  )

lazy val graboidProtocol = (project in file("graboid-protocol"))
  .settings(
    name           := "tremors-graboid-protocol",
    skip / publish := true,
    libraryDependencies ++= Seq(
      Dependencies.Borer
    ).flatten
  )
  .dependsOn(
    generator,
    generator % "test->test",
    core      % "test->test"
  )

lazy val graboid = (project in file("graboid"))
  .settings(
    name := "tremors-graboid",
    libraryDependencies ++= Seq(
      Dependencies.ZIO,
      Dependencies.ZIOStream,
      Dependencies.AaltoXML,
      Dependencies.Macwire,
      Dependencies.SweetMockito,
      Dependencies.FarangoDucktape
    ).flatten,
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
  )
  .dependsOn(
    core,
    core            % "test->test",
    quakeML,
    quakeML         % "test->test",
    zioStarter,
    zioKafka,
    zioKafka        % "test->test",
    zioFarango,
    generator,
    generator       % "test->test",
    graboidProtocol,
    graboidProtocol % "test->test",
    zioHttp
  )
  .enablePlugins(JavaAppPackaging, DockerPlugin)
  .enablePlugins(BuildInfoPlugin)
  .settings(
    buildInfoPackage := "graboid"
  )

lazy val graboidIt = (project in file("graboid-it"))
  .settings(
    name                     := "tremors-graboid-it",
    skip / publish           := true,
    parallelExecution / test := false,
    libraryDependencies ++= Seq(
      Dependencies.ZIOLogging
    ).flatten
  )
  .dependsOn(
    graboid,
    graboid    % "test->test",
    zioFarango % "test->test"
  )

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
      Dependencies.ZIOKafka,
      Dependencies.ZIOLogging,
      Dependencies.ZIOHttp,
      Dependencies.TestcontainersKafka,
      Dependencies.Macwire,
      Dependencies.SweetMockito,
      Dependencies.Borer,
      Dependencies.ZIOLogging.map(_.withConfigurations(Some("test")))
    ).flatten
  )
  .dependsOn(
    generator
  )

lazy val zioFarango = (project in file("zio/farango"))
  .settings(
    name := "tremors-zio-farango",
    libraryDependencies ++= Seq(
      Dependencies.ZIO,
      Dependencies.ZIOFarango,
      Dependencies.ZIOLogging,
      Dependencies.TestcontainersScala,
      Dependencies.FarangoDucktape
    ).flatten
  )

lazy val zioHttp = (project in file("zio/http"))
  .settings(
    name := "tremors-zio-http",
    resolvers += Resolver.defaultLocal,
    libraryDependencies ++= Seq(
      Dependencies.ZIO,
      Dependencies.ZIOStream,
      Dependencies.ZIOHttp
    ).flatten
  )
  .dependsOn(
    core
  )
