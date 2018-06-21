#!/bin/bash

chmod a+w /opt/openresty/nginx/conf/nginx.conf

sed -i "s|\${pacs_wado_uri}|$KHEOPS_PROXY_PACS_WADO_URI|" /opt/openresty/nginx/conf/nginx.conf
sed -i "s|\${pacs_wado_rs}|$KHEOPS_PROXY_PACS_WADO_RS|" /opt/openresty/nginx/conf/nginx.conf

if [ -f /run/secrets/kheops_auth_hmasecret ]; then
    filename="/run/secrets/kheops_auth_hmasecret"
    kheops_auth_hmasecret=$(head -n 1 $filename)
else
    echo "Missing kheops auth_hmasecret secret"
    kheops_auth_hmasecret=XXX
fi

export JWT_SECRET=$kheops_auth_hmasecret

nginx -g 'daemon off; error_log /dev/stderr info;'
