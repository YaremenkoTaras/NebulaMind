#!/bin/bash
# Maven wrapper with Java 21
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
mvn "$@"

