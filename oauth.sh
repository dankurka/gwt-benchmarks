#!/bin/bash -e

if [[ -z "${1}" || -z "${2}" ]]; then
  echo "usage: oauth.sh commitId <persistenceDir> <client_secret.json>" >&2
  exit 1
fi

echo "Starting build"
mvn -q install > /dev/null
pushd launcher
trap '{ echo "Hey, you pressed Ctrl-C.  Time to quit." ; popd; exit 0; }' INT
echo "Starting oauth tool"
mvn exec:java -Dexec.mainClass=com.google.gwt.benchmark.launcher.OAuthWriter -Dexec.args="${1} ${2}"

