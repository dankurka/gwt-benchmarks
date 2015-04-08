#!/bin/bash -e

echo "Starting build"
mvn -q clean install > /dev/null
pushd launcher
trap '{ echo "Hey, you pressed Ctrl-C.  Time to quit." ; popd; exit 0; }' INT
echo "Starting server"
mvn exec:exec \
    -Dexec.executable="java" \
    -Dexec.args="-classpath %classpath -DconfigFile=../../compileserver_config -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5555 com.google.gwt.benchmark.launcher.Launcher"

