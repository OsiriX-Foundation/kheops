#! /bin/bash

missing_env_var_secret=false

#Verify secrets
if ! [ -f /run/secrets/kheops_auth_hmasecret_post ]; then
    echo "Missing kheops_auth_hmasecret_post secret"
    missing_env_var_secret=true
fi
if ! [ -f /run/secrets/kheops_client_dicomwebproxysecret ]; then
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
if [ -z "$KHEOPS_ROOT_URL" ]; then
    echo "Missing $KHEOPS_ROOT_URL environment variable"
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
for f in /run/secrets/*
do
  filename=$(basename "$f")
  
  if [ "$filename" = "kubernetes.io" ]; then
    continue
  fi
  
  word_count=$(wc -w $f | cut -f1 -d" ")
  line_count=$(wc -l $f | cut -f1 -d" ")

  if [ ${word_count} != 1 ] || [ ${line_count} != 1 ]; then
    echo Error with secret $filename. He contains $word_count word and $line_count line
    exit 1
  fi
done

#######################################################################################
#ELASTIC SEARCH

if ! [ -z "$KHEOPS_DICOMWEB_PROXY_ENABLE_ELASTIC" ]; then
    if [ "$KHEOPS_DICOMWEB_PROXY_ENABLE_ELASTIC" = true ]; then

        echo "Start init filebeat"
        missing_env_var_secret=false

        if [[ -z $KHEOPS_DICOMWEB_PROXY_ELASTIC_INSTANCE ]]; then
          echo "Missing KHEOPS_DICOMWEB_PROXY_ELASTIC_INSTANCE environment variable"
          missing_env_var_secret=true
        else
           echo -e "environment variable KHEOPS_DICOMWEB_PROXY_ELASTIC_INSTANCE \e[92mOK\e[0m"
           sed -i "s|\${instance}|$KHEOPS_DICOMWEB_PROXY_ELASTIC_INSTANCE|" /etc/filebeat/filebeat.yml
        fi

        if [[ -z $KHEOPS_DICOMWEB_PROXY_LOGSTASH_URL ]]; then
          echo "Missing KHEOPS_DICOMWEB_PROXY_LOGSTASH_URL environment variable"
          missing_env_var_secret=true
        else
           echo -e "environment variable KHEOPS_DICOMWEB_PROXY_LOGSTASH_URL \e[92mOK\e[0m"
           sed -i "s|\${logstash_url}|$KHEOPS_DICOMWEB_PROXY_LOGSTASH_URL|" /etc/filebeat/filebeat.yml
        fi


        #if missing env var or secret => exit
        if [[ $missing_env_var_secret = true ]]; then
          exit 1
        else
           echo -e "all elastic secrets and all env vars \e[92mOK\e[0m"
        fi

        filebeat modules disable system
        service filebeat restart

        echo "Ending setup FILEBEAT"
    fi
else
    echo "[INFO] : Missing KHEOPS_DICOMWEB_PROXY_ENABLE_ELASTIC environment variable. Elastic is not enabled."
fi

#######################################################################################

exec "$@"
