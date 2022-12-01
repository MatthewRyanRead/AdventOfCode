#!/bin/bash

set -e

if [[ "$(echo $1 | grep -oE '^(10)|(1?[1-9])|(2[0-5])$')" != "$1" ]]; then
    echo 'Argument must be a valid advent day (between 1 and 25)'
    exit 1
fi

JAVA_MAJ_VER=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | grep -oE '^[0-9]+')
if (( $JAVA_MAJ_VER < 11 )); then
    echo 'Java 11+ is required'
    exit 1
fi

java --add-exports jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED "Day$1.java"
