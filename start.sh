#!/bin/bash -e

echo "Starting build"
mvn -q install > /dev/null
pushd launcher
trap '{ echo "Hey, you pressed Ctrl-C.  Time to quit." ; popd; exit 0; }' INT
echo "Starting server"
mvn -q exec:java -Dexec.mainClass=com.google.gwt.benchmark.launcher.Launcher \
    -DconfigFile=../../compileserver_config

