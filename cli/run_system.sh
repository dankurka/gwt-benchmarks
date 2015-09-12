#!/bin/bash -e

echo "Assuming build.sh ran successfully!"

quoted_args="$(printf " %q" "$@")"

mvn exec:java -Dexec.mainClass=com.google.j2cl.benchmark.cli.Cli -Dexec.args="$quoted_args"

#mvn exec:exec -Dexec.executable="java" \
#    -Dexec.args="-classpath %classpath -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=1044 com.google.j2cl.benchmark.cli.Cli $quoted_args"

