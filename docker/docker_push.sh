#!/bin/bash
echo "$DOCKER_PASSWORD" | docker login -u "$DOCKER_USERNAME" --password-stdin

mv build/libs/KheopsDICOMwebProxy.war docker/KheopsDICOMwebProxy.war

docker build ./docker/ --build-arg VCS_REF=`git rev-parse --short HEAD` -t osirixfoundation/kheopsproxy-tomcat:$BRANCH

docker push osirixfoundation/kheopsproxy-tomcat:$BRANCH
