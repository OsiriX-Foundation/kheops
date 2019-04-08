#!/bin/bash

missing_env_var_secret=false

#Verify secrets
if ! [ -f ${SECRET_FILE_PATH}/privkey1.pem ]; then
    echo "Missing privkey1.pem secret"
    missing_env_var_secret=true
else
   echo -e "secret privkey1.pem \e[92mOK\e[0m"
fi
if ! [ -f ${SECRET_FILE_PATH}/fullchain1.pem ]; then
    echo "Missing fullchain1.pem secret"
    missing_env_var_secret=true
else
   echo -e "secret fullchain1.pem \e[92mOK\e[0m"
fi


#Verify environment variables
if [[ -z $KHEOPS_ROOT_SCHEME ]]; then
  echo "Missing KHEOPS_ROOT_SCHEME environment variable"
  missing_env_var_secret=true
else
   echo -e "environment variable KHEOPS_ROOT_SCHEME \e[92mOK\e[0m"
fi

if [[ -z $KHEOPS_ROOT_HOST ]]; then
  echo "Missing KHEOPS_ROOT_HOST environment variable"
  missing_env_var_secret=true
else
   echo -e "environment variable KHEOPS_ROOT_HOST \e[92mOK\e[0m"
fi

if [[ -z $KHEOPS_DICOMWEB_PROXY_HOST ]]; then
  echo "Missing KHEOPS_DICOMWEB_PROXY_HOST environment variable"
  missing_env_var_secret=true
else
   echo -e "environment variable KHEOPS_DICOMWEB_PROXY_HOST \e[92mOK\e[0m"
fi

if [[ -z $KHEOPS_DICOMWEB_PROXY_PORT ]]; then
  echo "Missing KHEOPS_DICOMWEB_PROXY_PORT environment variable"
  missing_env_var_secret=true
else
   echo -e "environment variable KHEOPS_DICOMWEB_PROXY_PORT \e[92mOK\e[0m"
fi

if [[ -z $KHEOPS_AUTHORIZATION_HOST ]]; then
  echo "Missing KHEOPS_AUTHORIZATION_HOST environment variable"
  missing_env_var_secret=true
else
   echo -e "environment variable KHEOPS_AUTHORIZATION_HOST \e[92mOK\e[0m"
fi

if [[ -z $KHEOPS_AUTHORIZATION_PORT ]]; then
  echo "Missing KHEOPS_AUTHORIZATION_PORT environment variable"
  missing_env_var_secret=true
else
   echo -e "environment variable KHEOPS_AUTHORIZATION_PORT \e[92mOK\e[0m"
fi

if [[ -z $KHEOPS_PACS_PEP_HOST ]]; then
  echo "Missing KHEOPS_PACS_PEP_HOST environment variable"
  missing_env_var_secret=true
else
   echo -e "environment variable KHEOPS_PACS_PEP_HOST \e[92mOK\e[0m"
fi

if [[ -z $KHEOPS_PACS_PEP_PORT ]]; then
  echo "Missing KHEOPS_PACS_PEP_PORT environment variable"
  missing_env_var_secret=true
else
   echo -e "environment variable KHEOPS_PACS_PEP_PORT \e[92mOK\e[0m"
fi

if [[ -z $KHEOPS_ZIPPER_HOST ]]; then
  echo "Missing KHEOPS_ZIPPER_HOST environment variable"
  missing_env_var_secret=true
else
   echo -e "environment variable KHEOPS_ZIPPER_HOST \e[92mOK\e[0m"
fi

if [[ -z $KHEOPS_ZIPPER_PORT ]]; then
  echo "Missing KHEOPS_ZIPPER_PORT environment variable"
  missing_env_var_secret=true
else
   echo -e "environment variable KHEOPS_ZIPPER_PORT \e[92mOK\e[0m"
fi

if [[ -z $KHEOPS_UI_HOST ]]; then
  echo "Missing KHEOPS_UI_HOST environment variable"
  missing_env_var_secret=true
else
   echo -e "environment variable KHEOPS_UI_HOST \e[92mOK\e[0m"
fi

if [[ -z $KHEOPS_UI_PORT ]]; then
  echo "Missing KHEOPS_UI_PORT environment variable"
  missing_env_var_secret=true
else
   echo -e "environment variable KHEOPS_UI_PORT \e[92mOK\e[0m"
fi


#if missing env var or secret => exit
if [[ $missing_env_var_secret = true ]]; then
  exit 1
else
   echo -e "all nginx secrets and all env var \e[92mOK\e[0m"
fi

#get env var
chmod a+w /etc/nginx/conf.d/kheops.conf
sed -i "s|\${root_url}|$KHEOPS_ROOT_SCHEME://$KHEOPS_ROOT_HOST|" /etc/nginx/conf.d/kheops.conf

sed -i "s|\${DICOMWebProxy_url}|http://$KHEOPS_DICOMWEB_PROXY_HOST:$KHEOPS_DICOMWEB_PROXY_PORT|" /etc/nginx/conf.d/kheops.conf
sed -i "s|\${kheopsAuthorization_url}|http://$KHEOPS_AUTHORIZATION_HOST:$KHEOPS_AUTHORIZATION_PORT|" /etc/nginx/conf.d/kheops.conf
sed -i "s|\${kheopsAuthorizationProxy_url}|http://$KHEOPS_PACS_PEP_HOST:$KHEOPS_PACS_PEP_PORT|" /etc/nginx/conf.d/kheops.conf
sed -i "s|\${kheopsZipper_url}|http://$KHEOPS_ZIPPER_HOST:$KHEOPS_ZIPPER_PORT|" /etc/nginx/conf.d/kheops.conf
sed -i "s|\${kheopsWebUI_url}|http://$KHEOPS_UI_HOST:$KHEOPS_UI_PORT|" /etc/nginx/conf.d/kheops.conf

sed -i "s|\${server_name}|$KHEOPS_ROOT_HOST|" /etc/nginx/conf.d/kheops.conf

echo "Ending setup NGINX secrets and env var"

#######################################################################################
#ELASTIC SEARCH

if ! [ -z "$KHEOPS_REVERSE_PROXY_ENABLE_ELASTIC" ]; then
    if [ "$KHEOPS_REVERSE_PROXY_ENABLE_ELASTIC" = true ]; then

        echo "Start init metricbeat and filebeat"
        missing_env_var_secret=false

        #Verify secrets
        if ! [ -f ${SECRET_FILE_PATH}/elastic_pwd ]; then
            echo "Missing elastic_pwd"
            missing_env_var_secret=true
        else
           echo -e "secret elastic_pwd \e[92mOK\e[0m"
        fi


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

        if [[ -z $KHEOPS_REVERSE_PROXY_ELASTIC_USER ]]; then
          echo "Missing KHEOPS_REVERSE_PROXY_ELASTIC_USER environment variable"
          missing_env_var_secret=true
        else
           echo -e "environment variable KHEOPS_REVERSE_PROXY_ELASTIC_USER \e[92mOK\e[0m"
           sed -i "s|\${elastic_user}|$KHEOPS_REVERSE_PROXY_ELASTIC_USER|" /etc/metricbeat/metricbeat.yml
           sed -i "s|\${elastic_user}|$KHEOPS_REVERSE_PROXY_ELASTIC_USER|" /etc/filebeat/filebeat.yml
        fi

        if [[ -z $KHEOPS_REVERSE_PROXY_ELASTIC_URL ]]; then
          echo "Missing KHEOPS_REVERSE_PROXY_ELASTIC_URL environment variable"
          missing_env_var_secret=true
        else
           echo -e "environment variable KHEOPS_REVERSE_PROXY_ELASTIC_URL \e[92mOK\e[0m"
           sed -i "s|\${elastic_url}|$KHEOPS_REVERSE_PROXY_ELASTIC_URL|" /etc/metricbeat/metricbeat.yml
           sed -i "s|\${elastic_url}|$KHEOPS_REVERSE_PROXY_ELASTIC_URL|" /etc/filebeat/filebeat.yml
        fi

        if [[ -z $KHEOPS_REVERSE_PROXY_KIBANA_URL ]]; then
          echo "Missing KHEOPS_REVERSE_PROXY_KIBANA_URL environment variable"
          missing_env_var_secret=true
        else
           echo -e "environment variable KHEOPS_REVERSE_PROXY_KIBANA_URL \e[92mOK\e[0m"
           sed -i "s|\${kibana_url}|$KHEOPS_REVERSE_PROXY_KIBANA_URL|" /etc/metricbeat/metricbeat.yml
           sed -i "s|\${kibana_url}|$KHEOPS_REVERSE_PROXY_KIBANA_URL|" /etc/filebeat/filebeat.yml
        fi

        #if missing env var or secret => exit
        if [[ $missing_env_var_secret = true ]]; then
          exit 1
        else
           echo -e "all elastic secrets and all env var \e[92mOK\e[0m"
        fi

        metricbeat modules disable system
        filebeat modules disable system

        cat ${SECRET_FILE_PATH}/elastic_pwd | metricbeat keystore add ES_PWD --stdin --force
        service metricbeat restart
        cat ${SECRET_FILE_PATH}/elastic_pwd | filebeat keystore add ES_PWD --stdin --force
        service filebeat restart

        echo "Ending setup METRICBEAT and FILEBEAT"
    fi
else
    echo "[INFO] : Missing KHEOPS_REVERSE_PPROXY_ENABLE_ELASTIC environment variable. Elastic is not enable."
fi

#######################################################################################

nginx-debug -g 'daemon off;'
