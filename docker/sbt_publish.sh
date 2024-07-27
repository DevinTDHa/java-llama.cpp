#!/bin/bash

# Check if first argument is set
if [ -z "$1" ]; then
  echo "No extra flags set. Running publish for cpu."
else
  echo "Extra flags set. Running publish with $1."
fi

if [ -z "$PGP_SECRET" ]; then
  echo "PGP_SECRET is not set"
  exit 1
fi

if [ -z "$PGP_PASSPHRASE" ]; then
  echo "PGP_PASSPHRASE is not set"
  exit 1
fi

if [ -z "$SONATYPE_USERNAME" ]; then
  echo "SONATYPE_USERNAME is not set"
  exit 1
fi

if [ -z "$SONATYPE_PASSWORD" ]; then
  echo "SONATYPE_PASSWORD is not set"
  exit 1
fi

sbt "$1" "clean; compile; ci-release"
