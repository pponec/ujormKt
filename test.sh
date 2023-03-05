#!/bin/sh
# Java runtime version 17+ is required.

set -e
cd $(dirname $0)
./mvnw clean test