#!/bin/sh

DIR=/usr/share/nginx/html/js/
FILENAME=$(find /usr/share/nginx/html/js/ -name 'app.*.js')

missing_env_var_secret=false

#Verify environment variables
if [ -z "$KHEOPS_UI_TITLE" ]; then
    echo "Missing KHEOPS_UI_TITLE environment variable"
    missing_env_var_secret=true
fi

if [ -z "$KHEOPS_KEYCLOAK_URI" ]; then
    echo "Missing KHEOPS_KEYCLOAK_URI environment variable"
    missing_env_var_secret=true
fi

if [ -z "$KHEOPS_KEYCLOAK_REALMS" ]; then
    echo "Missing KHEOPS_KEYCLOAK_URI environment variable"
    missing_env_var_secret=true
fi

if [ -z "$KHEOPS_UI_KEYCLOAK_CLIENTID" ]; then
    echo "Missing KHEOPS_UI_KEYCLOAK_CLIENTID environment variable"
    missing_env_var_secret=true
fi

if [ -z "$KHEOPS_VIEWER_URL" ]; then
    echo "Missing KHEOPS_VIEWER_URL environment variable"
    missing_env_var_secret=true
fi

if [ -z "$KHEOPS_VIEWER_SM_URL" ]; then
    echo "Missing KHEOPS_VIEWER_SM_URL environment variable"
    missing_env_var_secret=true
fi

if [ -z "$KHEOPS_ROOT_SCHEME" ]; then
    echo "Missing KHEOPS_ROOT_SCHEME environment variable"
    missing_env_var_secret=true
fi

if [ -z "$KHEOPS_ROOT_HOST" ]; then
    echo "Missing KHEOPS_ROOT_HOST environment variable"
    missing_env_var_secret=true
fi

if [ -z "$KHEOPS_ROOT_PORT" ]; then
    echo "Missing KHEOPS_ROOT_PORT environment variable"
    missing_env_var_secret=true
fi

if [ -z "$KHEOPS_API_PATH" ]; then
    echo "Missing KHEOPS_API_PATH environment variable"
    missing_env_var_secret=true
fi

if [ "$missing_env_var_secret" = true ]; then
    exit 1
fi

sed -i "s|\%{kheops_ui_title}|$KHEOPS_UI_TITLE|g" $FILENAME
sed -i "s|\%{kheops_keycloak_uri}|$KHEOPS_KEYCLOAK_URI|g" $FILENAME
sed -i "s|\%{kheops_keycloak_realms}|$KHEOPS_KEYCLOAK_REALMS|g" $FILENAME
sed -i "s|\%{kheops_ui_keycloak_clientid}|$KHEOPS_UI_KEYCLOAK_CLIENTID|g" $FILENAME
api="${KHEOPS_ROOT_SCHEME}://${KHEOPS_ROOT_HOST}:${KHEOPS_ROOT_PORT}${KHEOPS_API_PATH}"
sed -i "s|\%{kheops_api_url}|$api|g" $FILENAME
sed -i "s|\%{kheops_viewer_url}|$KHEOPS_VIEWER_URL|g" $FILENAME
sed -i "s|\%{kheops_viewer_sm_url}|$KHEOPS_VIEWER_SM_URL|g" $FILENAME
root="${KHEOPS_ROOT_SCHEME}://${KHEOPS_ROOT_HOST}"
sed -i "s|\%{kheops_root_url}|$root|g" $FILENAME

chmod a+w /etc/nginx/conf.d/ui.conf

nginx -g 'daemon off;'
