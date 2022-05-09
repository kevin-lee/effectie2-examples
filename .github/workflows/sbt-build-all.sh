#!/bin/bash -e

set -x

if [ -z "$1" ]
  then
    echo "Missing parameters. Please enter the [Scala version]."
    echo "sbt-build-all.sh 3.0.0"
    exit 1
else
  scala_version=$1
  echo "============================================"
  echo "Build projects (All)"
  echo "--------------------------------------------"
  echo ""
  CURRENT_BRANCH_NAME="${GITHUB_REF#refs/heads/}"
  if [[ "$CURRENT_BRANCH_NAME" == "master" || "$CURRENT_BRANCH_NAME" == "release" ]]
  then
    sbt -J-Xmx2048m \
      ++${scala_version}! \
      -v \
      clean \
      test \
      packagedArtifacts
  else
    sbt -J-Xmx2048m \
      ++${scala_version}! \
      -v \
      clean \
      test \
      package
  fi

  echo "============================================"
  echo "Building projects (All): Done"
  echo "============================================"
fi
