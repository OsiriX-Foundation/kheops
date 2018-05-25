#!/bin/bash

echo $ROOT_URL
chmod a+w /etc/nginx/conf.d/myconf.conf
sed -i "s|\${root_url}|$ROOT_URL|" /etc/nginx/conf.d/myconf.conf

echo bonsoir
nginx -g 'daemon off;'
