#!/bin/bash

echo "$DOCKER_PASSWORD" | docker login -u "$DOCKER_USERNAME" --password-stdin

docker build ./docker/tls/ -t osirixfoundation/kheops-ui:tls
docker push osirixfoundation/kheops-ui:tls
