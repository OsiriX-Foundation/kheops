#!/bin/bash

chmod a+w /etc/nginx/conf.d/kheops.conf
sed -i "s|\${root_url}|$KHEOPS_ROOT_URL|" /etc/nginx/conf.d/kheops.conf

sed -i "s|\${kheopsviewer_url}|$KHEOPS_NGINX_URL_KHEOPSVIEWER|" /etc/nginx/conf.d/kheops.conf
sed -i "s|\${tomcat_url}|$KHEOPS_NGINX_URL_TOMCAT|" /etc/nginx/conf.d/kheops.conf
sed -i "s|\${wado_url}|$KHEOPS_WADO_ROOT|" /etc/nginx/conf.d/kheops.conf

https="https://"
server_name=${KHEOPS_ROOT_URL/$https/}

sed -i "s|\${server_name}|$server_name|" /etc/nginx/conf.d/kheops.conf

nginx -g 'daemon off;'

