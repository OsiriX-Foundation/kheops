#!/bin/bash
echo "$DOCKER_PASSWORD" | docker login -u "$DOCKER_USERNAME" --password-stdin

mv build/libs/KheopsZipper.war docker/KheopsZipper.war

docker build ./docker/ --build-arg VCS_REF=`git rev-parse --short HEAD` -t osirixfoundation/kheops-zipper:$BRANCH

docker push osirixfoundation/kheops-zipper:$BRANCH
