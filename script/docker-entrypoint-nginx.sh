#!/bin/sh

echo $TITLE

DIR=/usr/share/nginx/html/js/
FILENAME=$(find /usr/share/nginx/html/js/ -name 'app.*.js')
echo $FILENAME

sed -i "s|\${title}|$TITLE|" $FILENAME
sed -i "s|\${url_keycloak}|$URL_KEYCLOAK|" $FILENAME
sed -i "s|\${realm_keycloak}|$REALM_KEYCLOAK|" $FILENAME
sed -i "s|\${client_id}|$CLIENT_ID|" $FILENAME
sed -i "s|\${url_api}|$URL_API|" $FILENAME
sed -i "s|\${url_viewer}|$URL_VIEWER|" $FILENAME

chmod a+w /etc/nginx/conf.d/ui.conf
sed -i "s|\${server_name}|$SERVER_NAME|" /etc/nginx/conf.d/ui.conf

nginx-debug -g 'daemon off;'
