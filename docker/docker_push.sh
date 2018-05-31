#!/bin/bash
echo "$DOCKER_PASSWORD" | docker login -u "$DOCKER_USERNAME" --password-stdin

mv build/libs/KheopsProxy.war docker/KheopsProxy.war

docker build ./docker/ --build-arg VCS_REF=`git rev-parse --short HEAD` -t osirixfoundation/kheopsproxy-tomcat:$BRANCH

docker push osirixfoundation/kheopsproxy-tomcat:$BRANCH
