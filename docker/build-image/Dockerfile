FROM eclipse-temurin:17.0.5_8-jdk-alpine

ARG scala_version \
    sbt_version \
    sbt_home=/opt/sbt


# Install SBT
RUN apk --no-cache add wget bash git && \
    cd /tmp && \
    echo "Downloading sbt-${sbt_version}." && \
    wget --no-verbose "https://github.com/sbt/sbt/releases/download/v${sbt_version}/sbt-${sbt_version}.tgz" -O sbt.tar.gz && \
    echo "Installing sbt-${sbt_version}." && \
    tar xf sbt.tar.gz && \
    mkdir -p ${sbt_home} && \
    mv sbt/* ${sbt_home}/ && \
    ln -s ${sbt_home}/bin/sbt /usr/bin/sbt && \
    apk del wget && \
    echo "Creating sbt cache." && \
    sbt sbtVersion && \
    mkdir -p cache/project && \
    cd cache && \
    echo "scalaVersion := \"${scala_version}\"" > build.sbt && \
    echo "sbt.version=${sbt_version}" > project/build.properties && \
    echo "// force sbt compiler-bridge download" > project/Dependencies.scala && \
    echo "case object Temp" > Temp.scala && \
    echo "Compiling cache project." && \
    sbt compile && \
    echo "Testing cache project." && \
    sbt test && \
    echo "Package cache project." && \
    sbt package && \
    cd /tmp && \
    rm -rf cache && \
    echo "Done!"

COPY ./entrypoint.sh /
ENTRYPOINT  ["/entrypoint.sh"]
