#!/bin/bash

echo "$DOCKER_PASSWORD" | docker login -u "$DOCKER_USERNAME" --password-stdin

docker build ./docker/ -t osirixfoundation/kheops-ui:no-tls-nicolas
docker push osirixfoundation/kheops-ui:no-tls-nicolas
