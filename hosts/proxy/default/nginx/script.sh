#!/bin/bash

chmod a+w /opt/openresty/nginx/conf/nginx.conf

missing_env_var_secret=false

#Verify secrets
if [ -f /run/secrets/kheops_auth_hmasecret ]; then
    filename="/run/secrets/kheops_auth_hmasecret"
    word_count=$(wc -w $filename | cut -f1 -d" ")
    line_count=$(wc -l $filename | cut -f1 -d" ")

    if [ ${word_count} != 1 ] || [ ${line_count} != 1 ]; then
        echo Error with secret $filename. He contains $word_count word and $line_count line
        missing_env_var_secret=true
    fi
    kheops_auth_hmasecret=$(head -n 1 $filename)
else
    echo "Missing auth_hmasecret secret"
    missing_env_var_secret=true
fi

if [ -f /run/secrets/kheops_auth_hmasecret_post ]; then
    filename="/run/secrets/kheops_auth_hmasecret_post"
    word_count=$(wc -w $filename | cut -f1 -d" ")
    line_count=$(wc -l $filename | cut -f1 -d" ")

    if [ ${word_count} != 1 ] || [ ${line_count} != 1 ]; then
        echo Error with secret $filename. He contains $word_count word and $line_count line
        missing_env_var_secret=true
    fi
    kheops_auth_hmasecret_post=$(head -n 1 $filename)
else
    echo "Missing auth_hmasecret_post secret"
    missing_env_var_secret=true
fi

#Verify environment variables
if [ -z "$KHEOPS_PROXY_PACS_WADO_URI" ]; then
    echo "Missing KHEOPS_PROXY_PACS_WADO_URI environment variable"
    missing_env_var_secret=true
fi
if [ -z "$KHEOPS_PROXY_PACS_WADO_RS" ]; then
    echo "Missing KHEOPS_PROXY_PACS_WADO_RS environment variable"
    missing_env_var_secret=true
fi


#if missing env var or secret => exit
if [ "$missing_env_var_secret" = true ]; then
    exit 1
fi


#set env var
sed -i "s|\${pacs_wado_uri}|$KHEOPS_PROXY_PACS_WADO_URI|" /opt/openresty/nginx/conf/nginx.conf
sed -i "s|\${pacs_wado_rs}|$KHEOPS_PROXY_PACS_WADO_RS|" /opt/openresty/nginx/conf/nginx.conf

#set secrets
export JWT_SECRET=$kheops_auth_hmasecret
export JWT_POST_SECRET=$kheops_auth_hmasecret_post


echo "Ending setup PEP secrets and env var"

#######################################################################################
#ELASTIC SEARCH

if ! [ -z "$KHEOPS_PEP_ENABLE_ELASTIC" ]; then
    if [ "$KHEOPS_PEP_ENABLE_ELASTIC" = true ]; then

        echo "Start init metricbeat and filebeat"
        missing_env_var_secret=false

        #Verify secrets
        if ! [ -f /run/secrets/elastic_pwd ]; then
            echo "Missing elastic_pwd secret"
            missing_env_var_secret=true
        else
           echo "secret elastic_pwd OK"
        fi


        if [ -z $KHEOPS_PEP_ELASTIC_NAME ]; then
          echo "Missing KHEOPS_PEP_ELASTIC_NAME environment variable"
          missing_env_var_secret=true
        else
           echo -e "environment variable KHEOPS_PEP_ELASTIC_NAME OK"
           sed -i "s|\${elastic_name}|$KHEOPS_PEP_ELASTIC_NAME|" /etc/metricbeat/metricbeat.yml
           sed -i "s|\${elastic_name}|$KHEOPS_PEP_ELASTIC_NAME|" /etc/filebeat/filebeat.yml
        fi
        if [ -z $KHEOPS_PEP_ELASTIC_TAGS ]; then
          echo "Missing KHEOPS_PEP_ELASTIC_TAGS environment variable"
          missing_env_var_secret=true
        else
           echo -e "environment variable KHEOPS_PEP_ELASTIC_TAGS OK"
           sed -i "s|\${elastic_tags}|$KHEOPS_PEP_ELASTIC_TAGS|" /etc/metricbeat/metricbeat.yml
           sed -i "s|\${elastic_tags}|$KHEOPS_PEP_ELASTIC_TAGS|" /etc/filebeat/filebeat.yml
        fi

        if [ -z $KHEOPS_PEP_ELASTIC_USER ]; then
          echo "Missing KHEOPS_PEP_ELASTIC_USER environment variable"
          missing_env_var_secret=true
        else
           echo -e "environment variable KHEOPS_PEP_ELASTIC_USER OK"
           sed -i "s|\${elastic_user}|$KHEOPS_PEP_ELASTIC_USER|" /etc/metricbeat/metricbeat.yml
           sed -i "s|\${elastic_user}|$KHEOPS_PEP_ELASTIC_USER|" /etc/filebeat/filebeat.yml
        fi

        if [ -z $KHEOPS_PEP_ELASTIC_URL ]; then
          echo "Missing KHEOPS_PEP_ELASTIC_URL environment variable"
          missing_env_var_secret=true
        else
           echo -e "environment variable KHEOPS_PEP_ELASTIC_URL OK"
           sed -i "s|\${elastic_url}|$KHEOPS_PEP_ELASTIC_URL|" /etc/metricbeat/metricbeat.yml
           sed -i "s|\${elastic_url}|$KHEOPS_PEP_ELASTIC_URL|" /etc/filebeat/filebeat.yml
        fi

        if [ -z $KHEOPS_PEP_KIBANA_URL ]; then
          echo "Missing KHEOPS_PEP_KIBANA_URL environment variable"
          missing_env_var_secret=true
        else
           echo -e "environment variable KHEOPS_PEP_KIBANA_URL OK"
           sed -i "s|\${kibana_url}|$KHEOPS_PEP_KIBANA_URL|" /etc/metricbeat/metricbeat.yml
           sed -i "s|\${kibana_url}|$KHEOPS_PEP_KIBANA_URL|" /etc/filebeat/filebeat.yml
        fi


        #if missing env var or secret => exit
        if [ $missing_env_var_secret = true ]; then
          exit 1
        else
           echo "all elastic secrets and all env var OK"
        fi

        metricbeat modules disable system
        filebeat modules disable system

        cat /run/secrets/elastic_pwd | metricbeat keystore add ES_PWD --stdin --force
        service metricbeat restart
        cat /run/secrets/elastic_pwd | filebeat keystore add ES_PWD --stdin --force
        service filebeat restart

        echo "Ending setup METRICBEAT and FILEBEAT"
    fi
else
    echo "[INFO] : Missing KHEOPS_PEP_ENABLE_ELASTIC environment variable. Elastic is not enable."
fi

#######################################################################################


nginx -g 'daemon off; error_log /dev/stderr info;'
