#!/bin/bash


chmod a+w /etc/nginx/conf.d/myconf.conf
sed -i "s|\${root_url}|$ROOT_URL|" /etc/nginx/conf.d/myconf.conf

https="https://"
server_name=${$ROOT_URL/$https/}

sed -i "s|\${server_name}|$server_name|" /etc/nginx/conf.d/myconf.conf

nginx -g 'daemon off;'
