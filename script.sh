#!/bin/bash

chmod a+w /etc/nginx/conf.d/kheops.conf
sed -i "s|\${root_url}|$KHEOPS_ROOT_SCHEME://$KHEOPS_ROOT_HOST|" /etc/nginx/conf.d/kheops.conf

sed -i "s|\${DICOMWebProxy_url}|http://$KHEOPS_DICOMWEB_PROXY_HOST:$KHEOPS_DICOMWEB_PROXY_PORT|" /etc/nginx/conf.d/kheops.conf
sed -i "s|\${kheopsAuthorization_url}|http://$KHEOPS_AUTHORIZATION_HOST:$KHEOPS_AUTHORIZATION_PORT|" /etc/nginx/conf.d/kheops.conf
sed -i "s|\${kheopsAuthorizationProxy_url}|http://$KHEOPS_PEP_HOST:$KHEOPS_PEP_PORT|" /etc/nginx/conf.d/kheops.conf
sed -i "s|\${kheopsZipper_url}|http://$KHEOPS_ZIPPER_HOST:$KHEOPS_ZIPPER_PORT|" /etc/nginx/conf.d/kheops.conf
sed -i "s|\${kheopsWebUI_url}|http://$KHEOPS_VIEWER_HOST:$KHEOPS_VIEWER_PORT|" /etc/nginx/conf.d/kheops.conf


sed -i "s|\${server_name}|$KHEOPS_ROOT_HOST|" /etc/nginx/conf.d/kheops.conf

nginx-debug -g 'daemon off;'
