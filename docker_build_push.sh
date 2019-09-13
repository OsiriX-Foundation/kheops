#!/bin/bash

if [ -z $1 ]
then
	echo "missing tag"
	exit 1
fi

echo "build : osirixfoundation/kheops-reverse-proxy:$1"

docker build . -t osirixfoundation/kheops-reverse-proxy:$1

echo "push : osirixfoundation/kheops-reverse-proxy:$1"

docker push osirixfoundation/kheops-reverse-proxy:$1
