#! /bin/sh

missing_env_var_secret=false

#Verify secrets
if ! [ -f ${SECRET_FILE_PATH}/kheops_auth_hmasecret_post ]; then
    echo "Missing kheops_auth_hmasecret_post secret"
    missing_env_var_secret=true
fi
if ! [ -f ${SECRET_FILE_PATH}/kheops_client_dicomwebproxysecret ]; then
    echo "Missing kheops_client_dicomwebproxysecret secret"
    missing_env_var_secret=true
fi

#Verify environment variables
if [ -z "$KHEOPS_PACS_PEP_HOST" ]; then
    echo "Missing KHEOPS_PACS_PEP_HOST environment variable"
    missing_env_var_secret=true
fi
if [ -z "$KHEOPS_PACS_PEP_PORT" ]; then
    echo "Missing KHEOPS_PACS_PEP_PORT environment variable"
    missing_env_var_secret=true
fi
if [ -z "$KHEOPS_AUTHORIZATION_HOST" ]; then
    echo "Missing KHEOPS_AUTHORIZATION_HOST environment variable"
    missing_env_var_secret=true
fi
if [ -z "$KHEOPS_AUTHORIZATION_PORT" ]; then
    echo "Missing KHEOPS_AUTHORIZATION_PORT environment variable"
    missing_env_var_secret=true
fi
if [ -z "$KHEOPS_AUTHORIZATION_PATH" ]; then
    echo "Missing KHEOPS_AUTHORIZATION_PATH environment variable"
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
if [ -z "$KHEOPS_CLIENT_DICOMWEBPROXYCLIENTID" ]; then
    echo "Missing $KHEOPS_CLIENT_DICOMWEBPROXYCLIENTID environment variable"
    missing_env_var_secret=true
fi

#if missing env var or secret => exit
if [ "$missing_env_var_secret" = true ]; then
    exit 1
fi


#get secrets and verify content
for f in ${SECRET_FILE_PATH}/*
do
  word_count=$(wc -w $f | cut -f1 -d" ")
  line_count=$(wc -l $f | cut -f1 -d" ")

  filename=$(basename "$f")

  if [ ${word_count} != 1 ] || [ ${line_count} != 1 ]; then
    echo Error with secret $filename. He contains $word_count word and $line_count line
    exit 1
  fi
  
  value=$(cat ${f})
  sed -i "s|\${$filename}|$value|" ${REPLACE_FILE_PATH}
done


#get env var
sed -i "s|\${kheops_pacs_url}|http://$KHEOPS_PACS_PEP_HOST:$KHEOPS_PACS_PEP_PORT|" ${REPLACE_FILE_PATH}
sed -i "s|\${kheops_authorization_url}|http://$KHEOPS_AUTHORIZATION_HOST:$KHEOPS_AUTHORIZATION_PORT$KHEOPS_AUTHORIZATION_PATH|" ${REPLACE_FILE_PATH}
sed -i "s|\${kheops_client_dicomwebproxyclientid}|$KHEOPS_CLIENT_DICOMWEBPROXYCLIENTID|" ${REPLACE_FILE_PATH}

if { [ "$KHEOPS_ROOT_SCHEME" = "http" ] && [ "$KHEOPS_ROOT_PORT" = "80" ]; } || { [ "$KHEOPS_ROOT_SCHEME" = "https" ] && [ "$KHEOPS_ROOT_PORT" = "443" ]; }; then
    sed -i "s|\${kheops_root_url}|$KHEOPS_ROOT_SCHEME://$KHEOPS_ROOT_HOST|" ${REPLACE_FILE_PATH}
else
    sed -i "s|\${kheops_root_url}|$KHEOPS_ROOT_SCHEME://$KHEOPS_ROOT_HOST:$KHEOPS_ROOT_PORT|" ${REPLACE_FILE_PATH}
fi
echo "Ending setup NGINX secrets and env var"

#######################################################################################
#ELASTIC SEARCH

if ! [ -z "$KHEOPS_DICOMWEB_PROXY_ENABLE_ELASTIC" ]; then
    if [ "$KHEOPS_DICOMWEB_PROXY_ENABLE_ELASTIC" = true ]; then

        echo "Start init filebeat"
        missing_env_var_secret=false

        if [[ -z $KHEOPS_DICOMWEB_PROXY_ELASTIC_NAME ]]; then
          echo "Missing KHEOPS_DICOMWEB_PROXY_ELASTIC_NAME environment variable"
          missing_env_var_secret=true
        else
           echo -e "environment variable KHEOPS_DICOMWEB_PROXY_ELASTIC_NAME \e[92mOK\e[0m"
           sed -i "s|\${elastic_name}|$KHEOPS_DICOMWEB_PROXY_ELASTIC_NAME|" /etc/filebeat/filebeat.yml
        fi

        if [[ -z $KHEOPS_DICOMWEB_PROXY_ELASTIC_TAGS ]]; then
          echo "Missing KHEOPS_DICOMWEB_PROXY_ELASTIC_TAGS environment variable"
          missing_env_var_secret=true
        else
           echo -e "environment variable KHEOPS_DICOMWEB_PROXY_ELASTIC_TAGS \e[92mOK\e[0m"
           sed -i "s|\${elastic_tags}|$KHEOPS_DICOMWEB_PROXY_ELASTIC_TAGS|" /etc/filebeat/filebeat.yml
        fi

        if [[ -z $KHEOPS_DICOMWEB_PROXY_ELASTIC_URL ]]; then
          echo "Missing KHEOPS_DICOMWEB_PROXY_ELASTIC_URL environment variable"
          missing_env_var_secret=true
        else
           echo -e "environment variable KHEOPS_DICOMWEB_PROXY_ELASTIC_URL \e[92mOK\e[0m"
           sed -i "s|\${elastic_url}|$KHEOPS_DICOMWEB_PROXY_ELASTIC_URL|" /etc/filebeat/filebeat.yml
        fi

        if [[ -z $KHEOPS_DICOMWEB_PROXY_KIBANA_URL ]]; then
          echo "Missing KHEOPS_DICOMWEB_PROXY_KIBANA_URL environment variable"
          missing_env_var_secret=true
        else
           echo -e "environment variable KHEOPS_DICOMWEB_PROXY_KIBANA_URL \e[92mOK\e[0m"
           sed -i "s|\${kibana_url}|$KHEOPS_DICOMWEB_PROXY_KIBANA_URL|" /etc/filebeat/filebeat.yml
        fi

        #if missing env var or secret => exit
        if [[ $missing_env_var_secret = true ]]; then
          exit 1
        else
           echo -e "all elastic secrets and all env var \e[92mOK\e[0m"
        fi

        filebeat modules disable system
        service filebeat restart

        echo "Ending setup FILEBEAT"
    fi
else
    echo "[INFO] : Missing KHEOPS_DICOMWEB_PROXY_ENABLE_ELASTIC environment variable. Elastic is not enable."
fi

#######################################################################################

#run tomcat
catalina.sh run;
