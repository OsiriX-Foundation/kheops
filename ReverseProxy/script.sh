#!/bin/bash

missing_env_var_secret=false

#Verify environment variables
if [[ -z $KHEOPS_ROOT_URL ]]; then
  echo "Missing KHEOPS_ROOT_URL environment variable"
  missing_env_var_secret=true
else
   echo -e "environment variable KHEOPS_ROOT_URL \e[92mOK\e[0m"
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

if [[ -z $KHEOPS_OIDC_PROVIDER ]]; then
  echo "Missing KHEOPS_OIDC_PROVIDER environment variable"
  missing_env_var_secret=true
else
   echo -e "environment variable KHEOPS_OIDC_PROVIDER \e[92mOK\e[0m"
fi

if [[ -z $LETS_ENCRYPT_EMAIL ]]; then
  echo "Missing LETS_ENCRYPT_EMAIL environment variable"
  missing_env_var_secret=true
else
   echo -e "environment variable LETS_ENCRYPT_EMAIL \e[92mOK\e[0m"
fi

#if missing env var or secret => exit
if [[ $missing_env_var_secret = true ]]; then
  exit 1
else
   echo -e "all nginx secrets and all env var \e[92mOK\e[0m"
fi


# extract the protocol
proto="$(echo $KHEOPS_OIDC_PROVIDER | grep :// | sed -e's,^\(.*://\).*,\1,g')"
# remove the protocol
url="$(echo ${KHEOPS_OIDC_PROVIDER/$proto/})"
# extract the user (if any)
user="$(echo $url | grep @ | cut -d@ -f1)"
# extract the host and port
hostport="$(echo ${url/$user@/} | cut -d/ -f1)"
# by request host without port    
host="$(echo $hostport | sed -e 's,:.*,,g')"
# by request - try to extract the port
port="$(echo $hostport | sed -e 's,^.*:,:,g' -e 's,.*:\([0-9]*\).*,\1,g' -e 's,[^0-9],,g')"
# extract the path (if any)
path="$(echo $url | grep / | cut -d/ -f2-)"

export roothost="$(awk -F/ '{sub("^[^@]+@","",$3); print $3}' <<<$KHEOPS_ROOT_URL)"

#get env var
chmod a+w /etc/nginx/conf.d/kheops.conf
sed -i "s|\${root_url}|$KHEOPS_ROOT_URL|g" /etc/nginx/conf.d/kheops.conf

sed -i "s|\${DICOMWebProxy_url}|http://$KHEOPS_DICOMWEB_PROXY_HOST:$KHEOPS_DICOMWEB_PROXY_PORT|g" /etc/nginx/conf.d/kheops.conf
sed -i "s|\${kheopsAuthorization_url}|http://$KHEOPS_AUTHORIZATION_HOST:$KHEOPS_AUTHORIZATION_PORT|g" /etc/nginx/conf.d/kheops.conf
sed -i "s|\${kheopsAuthorizationProxy_url}|http://$KHEOPS_PACS_PEP_HOST:$KHEOPS_PACS_PEP_PORT|g" /etc/nginx/conf.d/kheops.conf
sed -i "s|\${kheopsZipper_url}|http://$KHEOPS_ZIPPER_HOST:$KHEOPS_ZIPPER_PORT|g" /etc/nginx/conf.d/kheops.conf
sed -i "s|\${kheopsWebUI_url}|http://$KHEOPS_UI_HOST:$KHEOPS_UI_PORT|g" /etc/nginx/conf.d/kheops.conf

sed -i "s|\${server_name}|$roothost|g" /etc/nginx/conf.d/kheops.conf
sed -i "s|\${keycloak_url}|$proto$hostport|g" /etc/nginx/conf.d/kheops.conf

echo "Ending setup NGINX secrets and env var"

#######################################################################################
#ELASTIC SEARCH

if ! [ -z "$KHEOPS_REVERSE_PROXY_ENABLE_ELASTIC" ]; then
    if [ "$KHEOPS_REVERSE_PROXY_ENABLE_ELASTIC" = true ]; then

        echo "Start init filebeat"
        missing_env_var_secret=false
        
       if [[ -z $KHEOPS_REVERSE_PROXY_ELASTIC_INSTANCE ]]; then
          echo "Missing KHEOPS_REVERSE_PROXY_ELASTIC_INSTANCE environment variable"
          missing_env_var_secret=true
        else
           echo -e "environment variable KHEOPS_REVERSE_PROXY_ELASTIC_INSTANCE \e[92mOK\e[0m"
           sed -i "s|\${instance}|$KHEOPS_REVERSE_PROXY_ELASTIC_INSTANCE|" /etc/filebeat/filebeat.yml
        fi

        if [[ -z $KHEOPS_REVERSE_PROXY_LOGSTASH_URL ]]; then
          echo "Missing KHEOPS_REVERSE_PROXY_LOGSTASH_URL environment variable"
          missing_env_var_secret=true
        else
           echo -e "environment variable KHEOPS_REVERSE_PROXY_LOGSTASH_URL \e[92mOK\e[0m"
           sed -i "s|\${logstash_url}|$KHEOPS_REVERSE_PROXY_LOGSTASH_URL|" /etc/filebeat/filebeat.yml
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
    echo "[INFO] : Missing KHEOPS_REVERSE_PPROXY_ENABLE_ELASTIC environment variable. Elastic is not enable."
fi

### Let's Encrypt
# based on https://ilhicas.com/2019/03/02/Nginx-Letsencrypt-Docker.html
# Create a self signed default certificate, so Ngix can start before we have
# any real certificates.

#Ensure we have folders available

if [[ ! -f /usr/share/nginx/certificates/fullchain.pem ]];then
    mkdir -p /usr/share/nginx/certificates
fi

### If we already have certbot generated certificates, copy them over
if [[ -f "${LETSENCRYPT_DIR:-/etc/letsencrypt}/live/$roothost/privkey.pem" ]]; then
    cp "${LETSENCRYPT_DIR:-/etc/letsencrypt}/live/$roothost/privkey.pem" /usr/share/nginx/certificates/privkey.pem
    cp "${LETSENCRYPT_DIR:-/etc/letsencrypt}/live/$roothost/fullchain.pem" /usr/share/nginx/certificates/fullchain.pem
    cp "${LETSENCRYPT_DIR:-/etc/letsencrypt}/live/$roothost/chain.pem" /usr/share/nginx/certificates/chain.pem
else
    openssl genrsa -out /usr/share/nginx/certificates/privkey.pem 4096
    openssl req -new -key /usr/share/nginx/certificates/privkey.pem -out /usr/share/nginx/certificates/cert.csr -nodes -subj \
    "/C=PT/ST=World/L=World/O=$roothost/OU=kheops lda/CN=${roothost}"
    openssl x509 -req -days 365 -in /usr/share/nginx/certificates/cert.csr -signkey /usr/share/nginx/certificates/privkey.pem -out /usr/share/nginx/certificates/fullchain.pem
    cp /usr/share/nginx/certificates/fullchain.pem /usr/share/nginx/certificates/chain.pem
fi

### Send certbot Emission/Renewal to background
$(while :; do /opt/certbot.sh; sleep "12h"; done;) &

### Check for changes in the certificate (i.e renewals or first start) and send this process to background
$(while inotifywait -e close_write /usr/share/nginx/certificates; do nginx -s reload; done) &


#######################################################################################

nginx -g 'daemon off;'
