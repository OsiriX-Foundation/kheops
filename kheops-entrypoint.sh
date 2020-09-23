#! /bin/bash

#######################################################################################
#ELASTIC SEARCH

if ! [ -z "$KHEOPS_AUTHORIZATION_ENABLE_ELASTIC" ]; then
    if [ "$KHEOPS_AUTHORIZATION_ENABLE_ELASTIC" = true ]; then

        echo "Start init filebeat and metricbeat"
        missing_env_var_secret=false

        if [ -z "$KHEOPS_AUTHORIZATION_ELASTIC_INSTANCE" ]; then
          echo "Missing KHEOPS_AUTHORIZATION_ELASTIC_INSTANCE environment variable"
          missing_env_var_secret=true
        else
           echo "environment variable KHEOPS_AUTHORIZATION_ELASTIC_INSTANCE \e[92mOK\e[0m"
           sed -i "s|\${instance}|$KHEOPS_AUTHORIZATION_ELASTIC_INSTANCE|" /etc/filebeat/filebeat.yml
           sed -i "s|\${instance}|$KHEOPS_AUTHORIZATION_ELASTIC_INSTANCE|" /etc/metricbeat/metricbeat.yml
        fi

        if [ -z "$KHEOPS_AUTHORIZATION_LOGSTASH_URL" ]; then
          echo "Missing KHEOPS_AUTHORIZATION_LOGSTASH_URL environment variable"
          missing_env_var_secret=true
        else
           echo "environment variable KHEOPS_AUTHORIZATION_LOGSTASH_URL \e[92mOK\e[0m"
           sed -i "s|\${logstash_url}|$KHEOPS_AUTHORIZATION_LOGSTASH_URL|" /etc/filebeat/filebeat.yml
           sed -i "s|\${logstash_url}|$KHEOPS_AUTHORIZATION_LOGSTASH_URL|" /etc/metricbeat/metricbeat.yml
        fi

        #if missing env var or secret => exit
        if [ $missing_env_var_secret = true ]; then
          exit 1
        else
           echo "all elastic secrets and all env var \e[92mOK\e[0m"
        fi

        filebeat modules disable system
        service filebeat restart
        metricbeat modules disable system
        service metricbeat restart

        echo "Ending setup FILEBEAT and METRICBEAT"
    fi
else
    echo "[INFO] : Missing KHEOPS_AUTHORIZATION_ENABLE_ELASTIC environment variable. Elastic is not enable."
fi

#######################################################################################

exec "$@"
