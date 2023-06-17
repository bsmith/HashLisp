#!/bin/sh

(./gradlew build installDist && build/install/jproto/bin/jproto)
