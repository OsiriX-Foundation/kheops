#! /bin/sh

missing_env_var_secret=false

#Verify secrets
if ! [ -f ${SECRET_FILE_PATH}/kheops_authdb_pass ]; then
    echo "Missing kheops_authdb_pass secret"
    missing_env_var_secret=true
fi
if ! [ -f ${SECRET_FILE_PATH}/kheops_superuser_hmasecret ]; then
    echo "Missing kheops_superuser_hmasecret secret"
    missing_env_var_secret=true
fi
if ! [ -f ${SECRET_FILE_PATH}/kheops_auth_hmasecret ]; then
    echo "Missing kheops kheops_auth_hmasecret secret"
    missing_env_var_secret=true
fi
if ! [ -f ${SECRET_FILE_PATH}/kheops_keycloak_clientsecret ]; then
    echo "Missing kheops_keycloak_clientsecret secret"
    missing_env_var_secret=true
fi
if ! [ -f ${SECRET_FILE_PATH}/kheops_client_dicomwebproxysecret ]; then
    echo "Missing kheops_client_dicomwebproxysecret secret"
    missing_env_var_secret=true
fi
if ! [ -f ${SECRET_FILE_PATH}/kheops_client_zippersecret ]; then
    echo "Missing kheops_client_zippersecret secret"
    missing_env_var_secret=true
fi

#Verify environment variables
if [ -z "$KHEOPS_AUTHDB_USER" ]; then
    echo "Missing KHEOPS_AUTHDB_USER environment variable"
    missing_env_var_secret=true
fi
if [ -z "$KHEOPS_AUTHDB_URL" ]; then
    echo "Missing KHEOPS_AUTHDB_URL environment variable"
    missing_env_var_secret=true
fi
if [ -z "$KHEOPS_AUTHDB_NAME" ]; then
    echo "Missing KHEOPS_AUTHDB_NAME environment variable"
    missing_env_var_secret=true
fi
if [ -z "KHEOPS_ROOT_SCHEME" ]; then
    echo "Missing KHEOPS_ROOT_SCHEME environment variable"
    missing_env_var_secret=true
fi
if [ -z "KHEOPS_ROOT_HOST" ]; then
    echo "Missing KHEOPS_ROOT_HOST environment variable"
    missing_env_var_secret=true
fi
if [ -z "KHEOPS_ROOT_PORT" ]; then
    echo "Missing KHEOPS_ROOT_PORT environment variable"
    missing_env_var_secret=true
fi
if [ -z "$KHEOPS_PACS_PEP_HOST" ]; then
    echo "Missing KHEOPS_PACS_PEP_HOST environment variable"
    missing_env_var_secret=true
fi
if [ -z "$KHEOPS_PACS_PEP_PORT" ]; then
    echo "Missing KHEOPS_PACS_PEP_PORT environment variable"
    missing_env_var_secret=true
fi
if [ -z "$KHEOPS_KEYCLOAK_URI" ]; then
    echo "Missing KHEOPS_KEYCLOAK_URI environment variable"
    missing_env_var_secret=true
fi
if [ -z "$KHEOPS_KEYCLOAK_CLIENTID" ]; then
    echo "Missing KHEOPS_KEYCLOAK_CLIENTID environment variable"
    missing_env_var_secret=true
fi
if [ -z "$KHEOPS_KEYCLOAK_REALMS" ]; then
    echo "Missing KHEOPS_KEYCLOAK_REALMS environment variable"
    missing_env_var_secret=true
fi
if [ -z "$KHEOPS_CLIENT_DICOMWEBPROXYCLIENTID" ]; then
    echo "Missing $KHEOPS_CLIENT_DICOMWEBPROXYCLIENTID environment variable"
    missing_env_var_secret=true
fi
if [ -z "$KHEOPS_CLIENT_ZIPPERCLIENTID" ]; then
    echo "Missing $KHEOPS_CLIENT_ZIPPERCLIENTID environment variable"
    missing_env_var=true
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
if { [ "$KHEOPS_ROOT_SCHEME" = "http" ] && [ "$KHEOPS_ROOT_PORT" = "80" ]; } || { [ "$KHEOPS_ROOT_SCHEME" = "https" ] && [ "$KHEOPS_ROOT_PORT" = "443" ]; }; then
    sed -i "s|\${kheops_root_url}|$KHEOPS_ROOT_SCHEME://$KHEOPS_ROOT_HOST|" ${REPLACE_FILE_PATH}
else
    sed -i "s|\${kheops_root_url}|$KHEOPS_ROOT_SCHEME://$KHEOPS_ROOT_HOST:$KHEOPS_ROOT_PORT|" ${REPLACE_FILE_PATH}
fi
sed -i "s|\${kheops_postgresql_user}|$KHEOPS_AUTHDB_USER|" ${REPLACE_FILE_PATH}
sed -i "s|\${kheops_postgresql_url}|$KHEOPS_AUTHDB_URL/$KHEOPS_AUTHDB_NAME|" ${REPLACE_FILE_PATH}
sed -i "s|\${kheops_pacs_url}|http://$KHEOPS_PACS_PEP_HOST:$KHEOPS_PACS_PEP_PORT|" ${REPLACE_FILE_PATH}

sed -i "s|\${kheops_keycloak_uri}|$KHEOPS_KEYCLOAK_URI|" ${REPLACE_FILE_PATH}
sed -i "s|\${kheops_keycloak_clientid}|$KHEOPS_KEYCLOAK_CLIENTID|" ${REPLACE_FILE_PATH}
sed -i "s|\${kheops_keycloak_realms}|$KHEOPS_KEYCLOAK_REALMS|" ${REPLACE_FILE_PATH}
sed -i "s|\${kheops_client_dicomwebproxyclientid}|$KHEOPS_CLIENT_DICOMWEBPROXYCLIENTID|" ${REPLACE_FILE_PATH}
sed -i "s|\${kheops_client_zipperclientid}|$KHEOPS_CLIENT_ZIPPERCLIENTID|" ${REPLACE_FILE_PATH}


echo "Ending setup NGINX secrets and env var"

#######################################################################################
#ELASTIC SEARCH

if ! [ -z "$KHEOPS_AUTHORIZATION_ENABLE_ELASTIC" ]; then
    if [ "$KHEOPS_AUTHORIZATION_ENABLE_ELASTIC" = true ]; then

        echo "Start init filebeat"
        missing_env_var_secret=false

        if [[ -z $KHEOPS_AUTHORIZATION_ELASTIC_NAME ]]; then
          echo "Missing KHEOPS_AUTHORIZATION_ELASTIC_NAME environment variable"
          missing_env_var_secret=true
        else
           echo -e "environment variable KHEOPS_AUTHORIZATION_ELASTIC_NAME \e[92mOK\e[0m"
           sed -i "s|\${elastic_name}|$KHEOPS_AUTHORIZATION_ELASTIC_NAME|" /etc/filebeat/filebeat.yml
        fi

        if [[ -z $KHEOPS_AUTHORIZATION_ELASTIC_TAGS ]]; then
          echo "Missing KHEOPS_AUTHORIZATION_ELASTIC_TAGS environment variable"
          missing_env_var_secret=true
        else
           echo -e "environment variable KHEOPS_AUTHORIZATION_ELASTIC_TAGS \e[92mOK\e[0m"
           sed -i "s|\${elastic_tags}|$KHEOPS_AUTHORIZATION_ELASTIC_TAGS|" /etc/filebeat/filebeat.yml
        fi

        if [[ -z $KHEOPS_AUTHORIZATION_ELASTIC_URL ]]; then
          echo "Missing KHEOPS_AUTHORIZATION_ELASTIC_URL environment variable"
          missing_env_var_secret=true
        else
           echo -e "environment variable KHEOPS_AUTHORIZATION_ELASTIC_URL \e[92mOK\e[0m"
           sed -i "s|\${elastic_url}|$KHEOPS_AUTHORIZATION_ELASTIC_URL|" /etc/filebeat/filebeat.yml
        fi

        if [[ -z $KHEOPS_AUTHORIZATION_KIBANA_URL ]]; then
          echo "Missing KHEOPS_AUTHORIZATION_KIBANA_URL environment variable"
          missing_env_var_secret=true
        else
           echo -e "environment variable KHEOPS_AUTHORIZATION_KIBANA_URL \e[92mOK\e[0m"
           sed -i "s|\${kibana_url}|$KHEOPS_AUTHORIZATION_KIBANA_URL|" /etc/filebeat/filebeat.yml
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
    echo "[INFO] : Missing KHEOPS_AUTHORIZATION_ENABLE_ELASTIC environment variable. Elastic is not enable."
fi

#######################################################################################


#run tomcat
catalina.sh run;
