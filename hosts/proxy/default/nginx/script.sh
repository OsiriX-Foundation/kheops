#!/bin/bash

chmod a+w /opt/openresty/nginx/conf/nginx.conf

sed -i "s|\${pacs_wado_uri}|$KHEOPS_PROXY_PACS_WADO_URI|" /opt/openresty/nginx/conf/nginx.conf
sed -i "s|\${pacs_wado_rs}|$KHEOPS_PROXY_PACS_WADO_RS|" /opt/openresty/nginx/conf/nginx.conf

if [ -f /run/secrets/kheops_superuser_hmasecret ]; then
    filename="/run/secrets/kheops_superuser_hmasecret"
    kheops_superuser_hmasecret=$(head -n 1 $filename)
else
    echo "Missing kheops superuser_hmasecret secret"
    kheops_superuser_hmasecret=XXX
fi

export JWT_SECRET=$kheops_superuser_hmasecret

nginx -g 'daemon off; error_log /dev/stderr info;'
