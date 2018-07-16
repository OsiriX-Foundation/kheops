#!/bin/bash

chmod a+w /etc/nginx/conf.d/kheops.conf
sed -i "s|\${root_url}|$KHEOPS_ROOT_URL|" /etc/nginx/conf.d/kheops.conf

sed -i "s|\${DICOMWebProxy_url}|$KHEOPS_NGINX_URL_DICOMWEB_PROXY|" /etc/nginx/conf.d/kheops.conf
sed -i "s|\${kheopsAuthorization_url}|$KHEOPS_NGINX_URL_AUTHORIZATION|" /etc/nginx/conf.d/kheops.conf
sed -i "s|\${kheopsAuthorizationProxy_url}|$KHEOPS_NGINX_URL_AUTHORIZATION_PROXY|" /etc/nginx/conf.d/kheops.conf
sed -i "s|\${kheopsZipper_url}|$KHEOPS_NGINX_URL_ZIPPER_PROXY|" /etc/nginx/conf.d/kheops.conf
sed -i "s|\${kheopsWebUI_url}|$KHEOPS_NGINX_URL_VIEWER|" /etc/nginx/conf.d/kheops.conf

https="https://"
server_name=${KHEOPS_ROOT_URL/$https/}

sed -i "s|\${server_name}|$server_name|" /etc/nginx/conf.d/kheops.conf

nginx -g 'daemon off;'
