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


missing_env_var_secret=false

#Verify secrets
if ! [ -f /run/secrets/elastic_cloud_id ]; then
    echo "Missing elastic_cloud_id secret"
    missing_env_var_secret=true
else
   echo -e "secret elastic_cloud_id \e[92mOK\e[0m"
fi

if ! [ -f /run/secrets/elastic_cloud_auth ]; then
    echo "Missing elastic_cloud_auth secret"
    missing_env_var_secret=true
else
   echo -e "secret elastic_cloud_authm \e[92mOK\e[0m"
fi


#get secrets and verify content
for f in /run/secrets/*
do
  filename=$(basename "$f")
  value=$(cat ${f})
  sed -i "s|\${$filename}|$value|" /etc/metricbeat/metricbeat.yml
  sed -i "s|\${$filename}|$value|" /etc/filebeat/filebeat.yml
done

if [[ -z $KHEOPS_PEP_ELASTIC_NAME ]]; then
  echo "Missing KHEOPS_PEP_ELASTIC_NAME environment variable"
  missing_env_var_secret=true
else
   echo -e "environment variable KHEOPS_PEP_ELASTIC_NAME \e[92mOK\e[0m"
   sed -i "s|\${elastic_name}|$KHEOPS_PEP_ELASTIC_NAME|" /etc/metricbeat/metricbeat.yml
   sed -i "s|\${elastic_name}|$KHEOPS_PEP_ELASTIC_NAME|" /etc/filebeat/filebeat.yml
fi
if [[ -z $KHEOPS_PEP_ELASTIC_TAGS ]]; then
  echo "Missing KHEOPS_PEP_ELASTIC_TAGS environment variable"
  missing_env_var_secret=true
else
   echo -e "environment variable KHEOPS_PEP_ELASTIC_TAGS \e[92mOK\e[0m"
   sed -i "s|\${elastic_tags}|$KHEOPS_PEP_ELASTIC_TAGS|" /etc/metricbeat/metricbeat.yml
   sed -i "s|\${elastic_tags}|$KHEOPS_PEP_ELASTIC_TAGS|" /etc/filebeat/filebeat.yml
fi

#if missing env var or secret => exit
if [[ $missing_env_var_secret = true ]]; then
  exit 1
else
   echo -e "all elastic secrets and all env var \e[92mOK\e[0m"
fi

metricbeat modules enable nginx
filebeat modules enable nginx
metricbeat modules disabled system
filebeat modules disabled system

service filebeat start
service metricbeat start

echo "Ending setup METRICBEAT and FILEBEAT"

#######################################################################################




nginx -g 'daemon off; error_log /dev/stderr info;'
