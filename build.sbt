ThisBuild / organization             := "one.estrondo"
ThisBuild / scalaVersion             := "3.6.4"
ThisBuild / version ~= (_.replace('+', '-'))
ThisBuild / dynver ~= (_.replace('+', '-'))
ThisBuild / resolvers ++= Resolver.sonatypeOssRepos("snapshots")
ThisBuild / Test / fork              := true
ThisBuild / Test / parallelExecution := false
ThisBuild / semanticdbEnabled        := true

val tremorsBaseImage  = "docker.io/eclipse-temurin:17-alpine"
val tremorsImageName  = "estrondo"
val tremorsRepository = Some("docker.io")

ThisBuild / scalacOptions ++= Seq(
  "-Wunused:all",
//  "-explain",
  "-deprecation",
  "-unchecked",
)

lazy val root = (project in file("."))
  .settings(
    name := "tremors",
  )
  .aggregate(
    core,
    quakeML,
    graboid,
    toph,
    tophIt,
    zioStarter,
    graboidIt,
    zioFarango,
    zioKafka,
    zioHttp,
  )

lazy val core = (project in file("core"))
  .settings(
    name := "tremors-core",
  )

lazy val generator = (project in file("generator"))
  .settings(
    name := "tremors-generator",
  )

lazy val quakeML = (project in file("quakeml"))
  .settings(
    name := "tremors-quakeml",
    libraryDependencies ++= Seq(
      Dependencies.Borer,
    ).flatten,
  )
  .dependsOn(
    generator % Test,
    core      % Test,
    core      % "test->test",
  )

lazy val graboidProtocol = (project in file("graboid-protocol"))
  .settings(
    name           := "tremors-graboid-protocol",
    skip / publish := true,
    libraryDependencies ++= Seq(
      Dependencies.Borer,
    ).flatten,
  )
  .dependsOn(
    generator % "compile->compile;test->test",
    core      % "test->test",
  )

lazy val graboid = (project in file("graboid"))
  .settings(
    name := "tremors-graboid",
    libraryDependencies ++= Seq(
      Dependencies.ZIO,
      Dependencies.ZIOStream,
      Dependencies.AaltoXML,
      Dependencies.SweetMockito,
      Dependencies.FarangoDucktape,
    ).flatten,
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework"),
  )
  .dependsOn(
    core            % "compile->compile;test->test",
    quakeML         % "compile->compile;test->test",
    zioStarter,
    zioKafka        % "compile->compile;test->test",
    zioFarango,
    generator       % "compile->compile;test->test",
    graboidProtocol % "compile->compile;test->test",
    zioHttp,
  )
  .enablePlugins(AshScriptPlugin, DockerPlugin)
  .settings(
    dockerRepository     := tremorsRepository,
    dockerBaseImage      := tremorsBaseImage,
    Docker / packageName := tremorsImageName,
    Docker / version ~= (x => "graboid-" + x.replace("+", "_")),
    dockerAliases += dockerAlias.value.withTag(Some("graboid-latest")),
  )
  .enablePlugins(BuildInfoPlugin)
  .settings(
    buildInfoPackage := "graboid",
  )

lazy val graboidIt = (project in file("graboid-it"))
  .settings(
    name                     := "tremors-graboid-it",
    skip / publish           := true,
    parallelExecution / test := false,
    libraryDependencies ++= Seq(
      Dependencies.ZIOLogging,
    ).flatten,
  )
  .dependsOn(
    graboid    % "compile->compile;test->test",
    zioFarango % "test->test",
  )

lazy val toph = (project in file("toph"))
  .settings(
    name                 := "tremors-toph",
    libraryDependencies ++= Seq(
      Dependencies.ZIO,
      Dependencies.ZIOStream,
      Dependencies.JWT,
      Dependencies.SweetMockito,
      Dependencies.gRPCTesting,
      Dependencies.JTS,
      Dependencies.Moidc4sZIO,
      Dependencies.Macwire,
    ).flatten,
    Compile / PB.targets := Seq(
      scalapb.gen(
        grpc = true,
        scala3Sources = true,
      )                                 -> (Compile / sourceManaged).value / "scalapb",
      scalapb.zio_grpc.ZioCodeGenerator -> (Compile / sourceManaged).value / "scalapb",
    ),
    libraryDependencies ++= Seq(
      "io.grpc"               % "grpc-netty"           % "1.65.1",
      "com.thesamet.scalapb" %% "scalapb-runtime-grpc" % scalapb.compiler.Version.scalapbVersion,
    ),
  )
  .dependsOn(
    zioStarter,
    zioKafka,
    zioFarango,
    zioHttp,
    quakeML % "compile->compile;test->test",
    core    % "test->test",
    zioGrpc % "compile->compile;test->test",
  )
  .enablePlugins(AshScriptPlugin, DockerPlugin)
  .settings(
    dockerRepository     := tremorsRepository,
    dockerBaseImage      := tremorsBaseImage,
    Docker / packageName := tremorsImageName,
    Docker / version ~= (x => "toph-" + x.replace("+", "_")),
    dockerAliases += dockerAlias.value.withTag(Some("toph-latest")),
  )
  .enablePlugins(BuildInfoPlugin)
  .settings(
    buildInfoPackage := "toph",
  )

lazy val tophIt = (project in file("toph-it"))
  .settings(
    name                     := "tremors-toph-it",
    skip / publish           := true,
    parallelExecution / test := false,
  )
  .dependsOn(
    toph       % "compile->compile;test->test",
    zioFarango % "test->test",
    zioGrpc    % "compile->compile;test->test",
  )

lazy val zioStarter = (project in file("zio/starter"))
  .settings(
    name := "tremors-zio-starter",
    libraryDependencies ++= Seq(
      Dependencies.ZIO,
      Dependencies.ZIOConfig,
      Dependencies.ZIOLogging,
    ).flatten,
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
      Dependencies.SweetMockito,
      Dependencies.Borer,
      Dependencies.ZIOLogging.map(_.withConfigurations(Some("test"))),
    ).flatten,
  )
  .dependsOn(
    generator,
  )

lazy val zioFarango = (project in file("zio/farango"))
  .settings(
    name := "tremors-zio-farango",
    libraryDependencies ++= Seq(
      Dependencies.ZIO,
      Dependencies.ZIOFarango,
      Dependencies.ZIOLogging,
      Dependencies.TestcontainersScala,
      Dependencies.FarangoDucktape,
      Dependencies.JTS,
    ).flatten,
  )

lazy val zioHttp = (project in file("zio/http"))
  .settings(
    name := "tremors-zio-http",
    resolvers += Resolver.defaultLocal,
    libraryDependencies ++= Seq(
      Dependencies.ZIO,
      Dependencies.ZIOStream,
      Dependencies.ZIOHttp,
      Dependencies.TestcontainersScala.map(_ % Test),
      Dependencies.Wiremock,
    ).flatten,
  )
  .dependsOn(
    core,
  )

lazy val zioGrpc = (project in file("zio/grpc"))
  .settings(
    name := "tremors-zio-grpc",
    resolvers += Resolver.defaultLocal,
    libraryDependencies ++= Seq(
      Dependencies.ZIO,
      Dependencies.gRPCTesting,
    ).flatten,
    libraryDependencies ++= Seq(
      "io.grpc"                        % "grpc-netty"           % "1.65.1",
      "com.thesamet.scalapb"          %% "scalapb-runtime-grpc" % scalapb.compiler.Version.scalapbVersion,
      "com.thesamet.scalapb.zio-grpc" %% "zio-grpc-core"        % Version.ZioGrpc,
    ),
  )
  .dependsOn(
    zioHttp % "compile->compile;test->test",
  )
