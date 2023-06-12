#!/bin/sh

set -e -x

mvn org.apache.maven.plugins:maven-dependency-plugin:2.8:get -Dartifact=org.junit.platform:junit-platform-console-standalone:1.9.3

mvn org.apache.maven.plugins:maven-dependency-plugin:2.8:copy -Dartifact=org.junit.platform:junit-platform-console-standalone:1.9.3 -DoutputDirectory=.

gradle classes testClasses

# XXX: default to --scan-classpath or something if no args provided
exec java -jar junit-platform-console-standalone-1.9.3.jar -cp build/classes/java/main -cp build/classes/java/test "$@"
