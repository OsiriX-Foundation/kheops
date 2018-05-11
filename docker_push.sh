#!/bin/bash
echo "$DOCKER_PASSWORD" | docker login -u "$DOCKER_USERNAME" --password-stdin

mv build/libs/KheopsAuthorization.war docker/KheopsAuthorization.war

docker build ./docker/ -t osirixfoundation/kheopsauthorization-tomcat
docker image ls

docker push osirixfoundation/kheopsauthorization-tomcat

find -name "*.war"
