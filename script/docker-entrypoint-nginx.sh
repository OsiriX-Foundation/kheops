#!/bin/sh

DIR=/usr/share/nginx/html/js/
FILENAME=$(find /usr/share/nginx/html/js/ -name 'app.*.js')

missing_env_var_secret=false

#Verify environment variables
if [ -z "$KHEOPS_UI_CLIENTID" ]; then
    echo "Missing KHEOPS_UI_CLIENTID environment variable"
    missing_env_var_secret=true
fi

if [ -z "$KHEOPS_OIDC_PROVIDER" ]; then
    echo "Missing KHEOPS_OIDC_PROVIDER environment variable"
    missing_env_var_secret=true
fi

if [ -z "$KHEOPS_ROOT_URL" ]; then
    echo "Missing KHEOPS_ROOT_URL environment variable"
    missing_env_var_secret=true
fi

if [ -z "$KHEOPS_API_URL" ]; then
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
    api="${KHEOPS_ROOT_SCHEME}://${KHEOPS_ROOT_HOST}:${KHEOPS_ROOT_PORT}${KHEOPS_API_PATH}"
else
    if [ -z "$KHEOPS_API_URL" ]; then
        echo "Missing KHEOPS_API_URL environment variable"
        missing_env_var_secret=true
    fi

    api="${KHEOPS_API_URL}"
fi

if [ "$missing_env_var_secret" = true ]; then
    exit 1
fi

sed -i "s|\%{kheops_ui_authority}|$KHEOPS_OIDC_PROVIDER|g" $FILENAME
sed -i "s|\%{kheops_ui_clientid}|$KHEOPS_UI_CLIENTID|g" $FILENAME
sed -i "s|\%{kheops_api_url}|$api|g" $FILENAME
sed -i "s|\%{kheops_ui_viewer_sm_url}|$KHEOPS_UI_VIEWER_SM_URL|g" $FILENAME
sed -i "s|\%{kheops_ui_root_url}|$KHEOPS_ROOT_URL|g" $FILENAME

if [ -z "$KHEOPS_UI_VIEWER_URL" ]; then
    KHEOPS_UI_VIEWER_URL=https://ohif.kheops.online
fi

if [ -z "$KHEOPS_UI_DISABLE_UPLOAD" ]; then
    KHEOPS_UI_DISABLE_UPLOAD=false
fi

if [ -z "$KHEOPS_UI_USER_MANAGEMENT_URL" ]; then
    KHEOPS_UI_USER_MANAGEMENT_URL=false
fi

sed -i "s|\%{kheops_ui_viewer_url}|$KHEOPS_UI_VIEWER_URL|g" $FILENAME
sed -i "s|\%{kheops_ui_user_management}|$KHEOPS_UI_USER_MANAGEMENT_URL|g" $FILENAME
sed -i "s|\%{kheops_ui_disable_upload}|$KHEOPS_UI_DISABLE_UPLOAD|g" $FILENAME

chmod a+w /etc/nginx/conf.d/ui.conf

nginx -g 'daemon off;'
