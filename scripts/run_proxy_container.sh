#!/bin/bash

set -o pipefail
set -e

echo -e "${cyan}RUN docker image${no_color}"
docker run --name "proxy-default" -d -p 80:80 --link backend:backend "osirixfoundation/pacsproxyauthorization"
