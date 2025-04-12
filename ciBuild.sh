#!/bin/bash
set -e

source build.properties
[ "$DEBUG" == 'true' ] && set -x # enable debugging?

# section to test prerequisites
declare -a VARS_NEEDED=("VERSION")
for i in "${VARS_NEEDED[@]}"; do
  if [[ -z "${!i}" ]]; then
    echo "Aborting! $i variable doesn't exist."
    exit 1
  fi
done

echo "Running build!"
git log -n3

if [[ $GIT_BRANCH == origin/release ]]; then
  echo "Running a CD build"

  # Setting new version number here
  export FULLVER=${VERSION}.${BUILD_NUMBER}
  mvn -B versions:set -DgenerateBackupPoms=false -DnewVersion=${FULLVER}

  # this pushes the snapshots to the s3 repo
  export MVN_TARGETS="clean verify install deploy"
  mvn -B $MVN_TARGETS $MAVEN_ARGS

  docker login -u $DOCKER_USR -p $DOCKER_PSW
  for i in "${DOCKERDIRS[@]}"; do
    # this pushes the docker image to docker hub
    cd $WORKSPACE/$i
    export MVN_TARGETS="dockerfile:build dockerfile:push"
    mvn -B $MVN_TARGETS $MAVEN_ARGS
    cd $WORKSPACE
  done
  docker logout

  # Make a new tag on the repository.
  export TAG=${GIT_BRANCH}_${FULLVER}
  git tag -m "Tagging release from CICD service" -a "${TAG}"
  git push origin tag ${TAG}
elif [[ $GIT_BRANCH == origin/master ]]; then
  echo "Running a CI build"

  # Setting new version number here
  export FULLVER=${VERSION}.${BUILD_NUMBER}-SNAPSHOT
  mvn -B versions:set -DgenerateBackupPoms=false -DnewVersion=${FULLVER}

  export MVN_TARGETS="clean verify install deploy"
  mvn -B $MVN_TARGETS $MAVEN_ARGS
else
  echo "Running a DEV build"

  # Setting new version number here
  export FULLVER=DEV-${VERSION}.${BUILD_NUMBER}
  mvn -B versions:set -DgenerateBackupPoms=false -DnewVersion=${FULLVER}

  export MVN_TARGETS="clean verify install"
  mvn -B $MVN_TARGETS $MAVEN_ARGS

  docker login -u $DOCKER_USR -p $DOCKER_PSW
  for i in "${DOCKERDIRS[@]}"; do
    # this pushes the docker image to docker hub
    cd $WORKSPACE/$i
    export MVN_TARGETS="dockerfile:build dockerfile:push"
    mvn -B $MVN_TARGETS $MAVEN_ARGS
    cd $WORKSPACE
  done
  docker logout
fi
