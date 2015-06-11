#!/bin/bash -e

echo "Assuming build.sh ran successfully!"

quoted_args="$(printf " %q" "$@")"

mvn exec:java -Dexec.mainClass=com.google.j2cl.benchmark.cli.Cli -Dexec.args="$quoted_args"

