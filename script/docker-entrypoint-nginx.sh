#!/bin/sh

DIR=/usr/share/nginx/html/js/
FILENAME=$(find /usr/share/nginx/html/js/ -name 'app.*.js')

sed -i "s|\${kheops_viewer_title}|$KHEOPS_VIEWER_TITLE|" $FILENAME
sed -i "s|\${kheops_keycloak_url}|$KHEOPS_KEYCLOAK_URL|" $FILENAME
sed -i "s|\${kheops_keycloak_realms}|$KHEOPS_KEYCLOAK_REALMS|" $FILENAME
sed -i "s|\${kheops_keycloak_viewer_clientid}|$KHEOPS_KEYCLOAK_VIEWER_CLIENTID|" $FILENAME
sed -i "s|\${kheops_url_api}|$KHEOPS_URL_API|" $FILENAME
sed -i "s|\${kheops_url_viewer}|$KHEOPS_URL_VIEWER|" $FILENAME

chmod a+w /etc/nginx/conf.d/ui.conf
sed -i "s|\${server_name}|$SERVER_NAME|" /etc/nginx/conf.d/ui.conf

nginx-debug -g 'daemon off;'
