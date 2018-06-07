#!/bin/bash

set -o pipefail
set -e

script_dir=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd)
. $script_dir/common.sh. 
. $script_dir/build_proxy_container.sh


echo -e "${cyan}RUN docker image${no_color}"
docker run --name "proxy-default" -d -p 80:80 --link backend:backend "osirixfoundation/pacsproxyauthorization"
