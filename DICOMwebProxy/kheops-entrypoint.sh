#! /bin/bash

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
