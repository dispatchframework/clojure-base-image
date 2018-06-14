#!/bin/sh
set -e -x

: ${DOCKER_REGISTRY:="dispatchframework"}

IMAGE=${DOCKER_REGISTRY}/clojure-base:$(cat version.txt)
docker build -t ${IMAGE} .
if [ -n "$PUSH" ]; then
    docker push ${IMAGE}
fi
