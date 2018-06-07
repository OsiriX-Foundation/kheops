#!/bin/bash

set -o pipefail
set -e

script_dir=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd)
. $script_dir/common.sh

proxy_dir=$proxy_base_dir/default

# make sure existing image/container is stopped/deleted
$script_dir/stop_proxy_container.sh

echo -e "${cyan}Building container and image for the proxy (Nginx) host...${no_color}"

echo -e "${blue}Deploying Lua scripts and depedencies${no_color}"
rm -rf $proxy_dir/nginx/lua
mkdir -p $proxy_dir/nginx/lua
cp $root_dir/nginx-jwt.lua $proxy_dir/nginx/lua
cp -r lib/* $proxy_dir/nginx/lua

echo -e "${blue}Building the new image${no_color}"
docker build -t="osirixfoundation/pacsproxyauthorization" --force-rm $proxy_dir
docker image ls
