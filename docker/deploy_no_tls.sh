#!/bin/bash

echo "$DOCKER_PASSWORD" | docker login -u "$DOCKER_USERNAME" --password-stdin

docker build ./docker/ -t osirixfoundation/viewer:no-tls
docker push osirixfoundation/viewer:no-tls
