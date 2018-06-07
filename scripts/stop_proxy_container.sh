#!/bin/bash

set -o pipefail
set -e

script_dir=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd)
. $script_dir/common.sh

proxy_dir=$proxy_base_dir/$proxy_name

echo -e "${cyan}Stopping the proxy container and removing the image${no_color}"
docker rm -f "proxy_default" &>/dev/null || true
docker rmi -f "osirixfoundation/pacsproxyauthorization" &>/dev/null || true
