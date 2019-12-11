#!/bin/bash
echo "$DOCKER_PASSWORD" | docker login -u "$DOCKER_USERNAME" --password-stdin

ls build
ls build/libs

mv build/libs/KheopsAuthorization.war docker/KheopsAuthorization.war
mv filebeat docker
mv metricbeat docker
mv liquibase docker

docker build ./docker/ --build-arg VCS_REF=`git rev-parse --short HEAD` -t osirixfoundation/kheops-authorization:$BRANCH

docker push osirixfoundation/kheops-authorization:$BRANCH
