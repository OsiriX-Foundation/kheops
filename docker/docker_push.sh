#!/bin/bash
echo "$DOCKER_PASSWORD" | docker login -u "$DOCKER_USERNAME" --password-stdin

mv build/libs/KheopsDICOMwebProxy.war docker/KheopsDICOMwebProxy.war
mv filebeat docker

docker build ./docker/ --build-arg VCS_REF=`git rev-parse --short HEAD` -t osirixfoundation/kheops-dicomweb-proxy:$BRANCH

docker push osirixfoundation/kheops-dicomweb-proxy:$BRANCH
