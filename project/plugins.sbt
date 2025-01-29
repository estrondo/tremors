addSbtPlugin("com.dwijnand"   % "sbt-dynver"          % "5.0.0-M3")
addSbtPlugin("com.eed3si9n"   % "sbt-buildinfo"       % "0.11.0")
addSbtPlugin("com.github.sbt" % "sbt-native-packager" % "1.11.1")
addSbtPlugin("org.scalameta"  % "sbt-scalafmt"        % "2.5.2")
addSbtPlugin("org.scoverage"  % "sbt-scoverage"       % "2.0.10")
addSbtPlugin("ch.epfl.scala"  % "sbt-scalafix"        % "0.12.1")

addDependencyTreePlugin

addSbtPlugin("com.thesamet" % "sbt-protoc" % "1.0.7")

libraryDependencies += "com.thesamet.scalapb.zio-grpc" %% "zio-grpc-codegen" % "0.6.2"
