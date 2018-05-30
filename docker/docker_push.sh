#!/bin/bash
echo "$DOCKER_PASSWORD" | docker login -u "$DOCKER_USERNAME" --password-stdin

mv build/libs/KheopsAuthorization.war docker/KheopsAuthorization.war

docker build ./docker/ --build-arg VCS_REF=`git rev-parse --short HEAD` -t osirixfoundation/kheopsauthorization-tomcat:$BRANCH

docker push osirixfoundation/kheopsauthorization-tomcat:$BRANCH
