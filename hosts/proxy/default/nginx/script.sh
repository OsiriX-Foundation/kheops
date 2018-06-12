#!/bin/bash

chmod a+w /opt/openresty/nginx/conf/nginx.conf

sed -i "s|\${pacs_wado_uri}|$KHEOPS_WADO_URI_ROOT|" /opt/openresty/nginx/conf/nginx.conf
sed -i "s|\${pacs_wado_rs}|$KHEOPS_PACS_URL|" /opt/openresty/nginx/conf/nginx.conf

nginx -g daemon off; error_log /dev/stderr info;
