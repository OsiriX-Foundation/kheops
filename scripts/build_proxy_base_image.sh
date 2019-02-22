#!/bin/bash

set -o pipefail
set -e

script_dir=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd)
. $script_dir/common.sh

# use sha-1 digest of base image Dockerfle as its Docker image tag, so we build a new base image whenever it changes
dockerfile_sha1=$(cat $proxy_base_dir/Dockerfile | openssl sha1 | sed 's/^.* //')
echo -e "${cyan}Required Dockerfile SHA1:${no_color} $dockerfile_sha1"

echo -e "${cyan}Building base proxy image${no_color}"

docker build --build-arg VCS_REF=`git rev-parse --short HEAD` -t="osirixfoundation/pacs-authorization-proxy:$BRANCH" --force-rm $proxy_base_dir

