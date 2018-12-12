#!/bin/bash

echo "$DOCKER_PASSWORD" | docker login -u "$DOCKER_USERNAME" --password-stdin

docker build ./docker/tls/ -t osirixfoundation/kheopsUI:tls
docker push osirixfoundation/kheopsUI:tls
