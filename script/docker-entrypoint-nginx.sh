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

if [ -z "$KHEOPS_ROOT_SCHEME" ]; then
    echo "Missing KHEOPS_ROOT_SCHEME environment variable"
    missing_env_var_secret=true
fi

if [ -z "$KHEOPS_ROOT_HOST" ]; then
    echo "Missing KHEOPS_ROOT_HOST environment variable"
    missing_env_var_secret=true
fi

if [ -z "$KHEOPS_API_PATH" ]; then
    echo "Missing KHEOPS_API_PATH environment variable"
    missing_env_var_secret=true
fi

if [ "$missing_env_var_secret" = true ]; then
    exit 1
fi

sed -i "s|\${kheops_ui_title}|$KHEOPS_UI_TITLE|g" $FILENAME
sed -i "s|\${kheops_keycloak_uri}|$KHEOPS_KEYCLOAK_URI|g" $FILENAME
sed -i "s|\${kheops_keycloak_realms}|$KHEOPS_KEYCLOAK_REALMS|g" $FILENAME
sed -i "s|\${kheops_ui_keycloak_clientid}|$KHEOPS_UI_KEYCLOAK_CLIENTID|g" $FILENAME
api="${KHEOPS_ROOT_SCHEME}://${KHEOPS_ROOT_HOST}${KHEOPS_API_PATH}"
sed -i "s|\${kheops_api_url}|$api|g" $FILENAME
sed -i "s|\${kheops_viewer_url}|$KHEOPS_VIEWER_URL|g" $FILENAME

chmod a+w /etc/nginx/conf.d/ui.conf

#######################################################################################
#ELASTIC SEARCH

if ! [ -z "$KHEOPS_REVERSE_PROXY_ENABLE_ELASTIC" ]; then
    if [ "$KHEOPS_REVERSE_PROXY_ENABLE_ELASTIC" = true ]; then
        missing_env_var_secret=false

        #Verify secrets
        if ! [ -f ${SECRET_FILE_PATH}/elastic_cloud_id ]; then
            echo "Missing elastic_cloud_id secret"
            missing_env_var_secret=true
        else
           echo -e "secret elastic_cloud_id \e[92mOK\e[0m"
        fi

        if ! [ -f ${SECRET_FILE_PATH}/elastic_cloud_auth ]; then
            echo "Missing elastic_cloud_auth secret"
            missing_env_var_secret=true
        else
           echo -e "secret elastic_cloud_authm \e[92mOK\e[0m"
        fi


        #get secrets and verify content
        for f in ${SECRET_FILE_PATH}/*
        do
          filename=$(basename "$f")
          value=$(cat ${f})
          sed -i "s|\${$filename}|$value|" /etc/metricbeat/metricbeat.yml
          sed -i "s|\${$filename}|$value|" /etc/filebeat/filebeat.yml
        done

        if [[ -z $KHEOPS_REVERSE_PROXY_ELASTIC_NAME ]]; then
          echo "Missing KHEOPS_REVERSE_PROXY_ELASTIC_NAME environment variable"
          missing_env_var_secret=true
        else
           echo -e "environment variable KHEOPS_REVERSE_PROXY_ELASTIC_NAME \e[92mOK\e[0m"
           sed -i "s|\${elastic_name}|$KHEOPS_REVERSE_PROXY_ELASTIC_NAME|" /etc/metricbeat/metricbeat.yml
           sed -i "s|\${elastic_name}|$KHEOPS_REVERSE_PROXY_ELASTIC_NAME|" /etc/filebeat/filebeat.yml
        fi
        if [[ -z $KHEOPS_REVERSE_PROXY_ELASTIC_TAGS ]]; then
          echo "Missing KHEOPS_REVERSE_PROXY_ELASTIC_TAGS environment variable"
          missing_env_var_secret=true
        else
           echo -e "environment variable KHEOPS_REVERSE_PROXY_ELASTIC_TAGS \e[92mOK\e[0m"
           sed -i "s|\${elastic_tags}|$KHEOPS_REVERSE_PROXY_ELASTIC_TAGS|" /etc/metricbeat/metricbeat.yml
           sed -i "s|\${elastic_tags}|$KHEOPS_REVERSE_PROXY_ELASTIC_TAGS|" /etc/filebeat/filebeat.yml
        fi

        #if missing env var or secret => exit
        if [[ $missing_env_var_secret = true ]]; then
          exit 1
        else
           echo -e "all elastic secrets and all env var \e[92mOK\e[0m"
        fi

        metricbeat modules enable nginx
        filebeat modules enable nginx
        metricbeat modules disable system
        filebeat modules disable system

        service filebeat start
        service metricbeat start

        echo "Ending setup METRICBEAT and FILEBEAT"
    fi
else
    echo "[INFO] : Missing KHEOPS_REVERSE_PPROXY_ENABLE_ELASTIC environment variable. Elastic is not enable."
fi
#######################################################################################

nginx-debug -g 'daemon off;'
