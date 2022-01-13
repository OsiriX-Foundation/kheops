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
    if [ -z "$KHEOPS_API_PATH" ]; then
        echo "Missing KHEOPS_API_PATH environment variable"
        missing_env_var_secret=true
    fi
    api="${KHEOPS_ROOT_URL}${KHEOPS_API_PATH}"
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
sed -i "s|\%{kheops_ui_additional_oidc_options}|${KHEOPS_UI_ADDITIONAL_OIDC_OPTIONS//\"/\\\\\"}|g" $FILENAME
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

if [ -z "$KHEOPS_UI_DISABLE_AUTOCOMPLET" ]; then
    KHEOPS_UI_DISABLE_AUTOCOMPLET=false
fi

if [ -z "$SHOW_KHEOPS_UI_DELETE_CONTACT" ]; then
    SHOW_KHEOPS_UI_DELETE_CONTACT=false
fi

sed -i "s|\%{kheops_ui_viewer_url}|$KHEOPS_UI_VIEWER_URL|g" $FILENAME
sed -i "s|\%{kheops_ui_user_management}|$KHEOPS_UI_USER_MANAGEMENT_URL|g" $FILENAME
sed -i "s|\%{kheops_ui_disable_upload}|$KHEOPS_UI_DISABLE_UPLOAD|g" $FILENAME
sed -i "s|\%{kheops_ui_disable_autocomplet}|$KHEOPS_UI_DISABLE_AUTOCOMPLET|g" $FILENAME
sed -i "s|\%{kheops_ui_show_delete_contact}|$KHEOPS_UI_SHOW_DELETE_CONTACT|g" $FILENAME
sed -i "s|\%{kheops_ui_delete_contact}|$KHEOPS_UI_DELETE_CONTACT|g" $FILENAME

NGINX_FILENAME=/etc/nginx/templates/default.conf.template

# extract the protocol
proto="$(echo $KHEOPS_OIDC_PROVIDER | grep :// | sed -e's,^\(.*://\).*,\1,g')"
# remove the protocol
url="$(echo ${KHEOPS_OIDC_PROVIDER/$proto/})"
# extract the user (if any)
user="$(echo $url | grep @ | cut -d@ -f1)"
# extract the host and port
hostport="$(echo ${url/$user@/} | cut -d/ -f1)"
# by request host without port
host="$(echo $hostport | sed -e 's,:.*,,g')"
# by request - try to extract the port
port="$(echo $hostport | sed -e 's,^.*:,:,g' -e 's,.*:\([0-9]*\).*,\1,g' -e 's,[^0-9],,g')"
[[ ! -z ${port} ]] && port=":$port";

KHEOPS_ROOT_OIDC=$proto$host$port

sed -i "s|\%{kheops_root_oidc}|$KHEOPS_ROOT_OIDC|g" $NGINX_FILENAME
