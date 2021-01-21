#!/bin/bash

missing_env_var_secret=false

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

if [[ -z $KHEOPS_INSTANCES ]]; then
  echo "Missing KHEOPS_INSTANCES environment variable"
  missing_env_var_secret=true
else
   echo -e "environment variable KHEOPS_INSTANCES \e[92mOK\e[0m"
fi

if [[ -z $KHEOPS_AUTHORIZATION_METRICBEAT_LOGSTASH_URL ]]; then
  echo "Missing KHEOPS_AUTHORIZATION_LOGSTASH_URL environment variable"
  missing_env_var_secret=true
else
   echo -e "environment variable KHEOPS_AUTHORIZATION_LOGSTASH_URL \e[92mOK\e[0m"
fi

#if missing env var or secret => exit
if [[ $missing_env_var_secret = true ]]; then
  exit 1
else
   echo -e "all env var \e[92mOK\e[0m"
fi

sed -i "s|\${kheops_authorization_url}|http://$KHEOPS_AUTHORIZATION_HOST:$KHEOPS_AUTHORIZATION_PORT|g" /etc/metricbeat/modules.d/http.yml
sed -i "s|\${instance}|http://$KHEOPS_INSTANCES|g" /etc/metricbeat/metricbeat.yml
sed -i "s|\${logstash_url}|http://$KHEOPS_AUTHORIZATION_METRICBEAT_LOGSTASH_URL|g" /etc/metricbeat/metricbeat.yml

echo "Ending setup NGINX secrets and env var"
