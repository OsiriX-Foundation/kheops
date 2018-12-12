#!/bin/bash

echo "$DOCKER_PASSWORD" | docker login -u "$DOCKER_USERNAME" --password-stdin

docker build ./docker/ -t osirixfoundation/kheopsUI:no-tls
docker push osirixfoundation/kheopsUI:no-tls
