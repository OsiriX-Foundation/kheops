#!/bin/bash
echo "$DOCKER_PASSWORD" | docker login -u "$DOCKER_USERNAME" --password-stdin
docker build ./docker/ -t test/test
docker image ls
#.build/libs/KheopsAuthorization.war
find -name "*.war"
