#!/bin/sh

SCALA_VERSION="$(sed -n -r '/scalaVersion/s/.*"([^"]+)".*/\1/p' ../../build.sbt)"
SBT_VERSION="$(sed -n -r '/sbt\.version=/s/.*=(.+)/\1/p' ../../project/build.properties)"

docker build -t estrondo/tremors-build-image:$SCALA_VERSION-$SBT_VERSION --build-arg scala_version=$SCALA_VERSION --build-arg sbt_version=$SBT_VERSION .