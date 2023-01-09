#!/usr/bin/env bash
sbt clean compile coverage scalastyle scalafmtAll test:scalafmtAll it:test::scalafmtAll test it:test coverageReport
