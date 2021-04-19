#!/bin/bash

missing_env_var_secret=false

if [[ -z $KHEOPS_INSTANCES ]]; then
  echo "Missing KHEOPS_INSTANCES environment variable"
  missing_env_var_secret=true
else
   echo -e "environment variable KHEOPS_INSTANCES \e[92mOK\e[0m"
fi

if [[ -z $KHEOPS_LOGSTASH_URL ]]; then
  echo "Missing KHEOPS_LOGSTASH_URL environment variable"
  missing_env_var_secret=true
else
   echo -e "environment variable KHEOPS_LOGSTASH_URL \e[92mOK\e[0m"
fi

#if missing env var or secret => exit
if [[ $missing_env_var_secret = true ]]; then
  exit 1
else
   echo -e "all env var \e[92mOK\e[0m"
fi


sed -i "s|\${instance}|$KHEOPS_INSTANCES|g" /filebeattmp/filebeat.yml
sed -i "s|\${logstash_url}|$KHEOPS_LOGSTASH_URL|g" /filebeattmp/filebeat.yml

echo "Ending setup env var"

mv /filebeattmp/filebeat.yml /usr/share/filebeat/filebeat.yml

chmod 640 /usr/share/filebeat/filebeat.yml

filebeat modules disable system

exec /usr/local/bin/docker-entrypoint -e
