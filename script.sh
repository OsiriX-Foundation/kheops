#!/bin/bash

missing_env_var_secret=false

#Verify secrets
if ! [ -f ${SECRET_FILE_PATH}/privkey1.pem ]; then
    echo "Missing privkey1.pem secret"
    missing_env_var_secret=true
fi
if ! [ -f ${SECRET_FILE_PATH}/fullchain1.pem ]; then
    echo "Missing fullchain1.pem secret"
    missing_env_var_secret=true
fi


#Verify environment variables
if [[ -z $KHEOPS_ROOT_SCHEME ]]; then
  echo "Missing KHEOPS_ROOT_SCHEME environment variable"
  missing_env_var_secret=true
fi
if [[ -z $KHEOPS_ROOT_HOST ]]; then
  echo "Missing KHEOPS_ROOT_HOST environment variable"
  missing_env_var_secret=true
fi
if [[ -z $KHEOPS_DICOMWEB_PROXY_HOST ]]; then
  echo "Missing KHEOPS_DICOMWEB_PROXY_HOST environment variable"
  missing_env_var_secret=true
fi
if [[ -z $KHEOPS_DICOMWEB_PROXY_PORT ]]; then
  echo "Missing KHEOPS_DICOMWEB_PROXY_PORT environment variable"
  missing_env_var_secret=true
fi
if [[ -z $KHEOPS_AUTHORIZATION_HOST ]]; then
  echo "Missing KHEOPS_AUTHORIZATION_HOST environment variable"
  missing_env_var_secret=true
fi
if [[ -z $KHEOPS_AUTHORIZATION_PORT ]]; then
  echo "Missing KHEOPS_AUTHORIZATION_PORT environment variable"
  missing_env_var_secret=true
fi
if [[ -z $KHEOPS_PACS_PEP_HOST ]]; then
  echo "Missing KHEOPS_PACS_PEP_HOST environment variable"
  missing_env_var_secret=true
fi
if [[ -z $KHEOPS_PACS_PEP_PORT ]]; then
  echo "Missing KHEOPS_PACS_PEP_PORT environment variable"
  missing_env_var_secret=true
fi
if [[ -z $KHEOPS_ZIPPER_HOST ]]; then
  echo "Missing KHEOPS_ZIPPER_HOST environment variable"
  missing_env_var_secret=true
fi
if [[ -z $KHEOPS_ZIPPER_PORT ]]; then
  echo "Missing KHEOPS_ZIPPER_PORT environment variable"
  missing_env_var_secret=true
fi
if [[ -z $KHEOPS_UI_HOST ]]; then
  echo "Missing KHEOPS_UI_HOST environment variable"
  missing_env_var_secret=true
fi
if [[ -z $KHEOPS_UI_PORT ]]; then
  echo "Missing KHEOPS_UI_PORT environment variable"
  missing_env_var_secret=true
fi


#if missing env var or secret => exit
if [[ missing_env_var_secret=true ]]; then
  exit 1
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

nginx-debug -g 'daemon off;'
