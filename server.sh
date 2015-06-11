#!/bin/bash -e

echo "Starting build"
mvn -q clean install > /dev/null
pushd server
trap '{ echo "Hey, you pressed Ctrl-C.  Time to quit." ; popd; exit 0; }' INT
echo "Starting server"
mvn jetty:run \
    -DconfigFile=./config/config
