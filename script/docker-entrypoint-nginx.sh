#!/bin/sh

DIR=/usr/share/nginx/html/js/
FILENAME=$(find /usr/share/nginx/html/js/ -name 'app.*.js')

sed -i "s|\${kheops_ui_title}|$KHEOPS_UI_TITLE|" $FILENAME
sed -i "s|\${kheops_keycloak_uri}|$KHEOPS_KEYCLOAK_URI|" $FILENAME
sed -i "s|\${kheops_keycloak_realms}|$KHEOPS_KEYCLOAK_REALMS|" $FILENAME
sed -i "s|\${kheops_ui_keycloak_clientid}|$KHEOPS_UI_KEYCLOAK_CLIENTID|" $FILENAME
sed -i "s|\${kheops_api_url}|$KHEOPS_API_URL|" $FILENAME
sed -i "s|\${kheops_viewer_url}|$KHEOPS_VIEWER_URL|" $FILENAME

chmod a+w /etc/nginx/conf.d/ui.conf
sed -i "s|\${server_name}|$SERVER_NAME|" /etc/nginx/conf.d/ui.conf

nginx-debug -g 'daemon off;'
