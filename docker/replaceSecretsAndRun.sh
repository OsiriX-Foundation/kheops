#! /bin/sh

missing_env_var_secret=false

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
if ! [ -f ${SECRET_FILE_PATH}/kheops_keycloak_password ]; then
    echo "Missing kheops_keycloak_password secret"
    missing_env_var_secret=true
fi



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
if [ -z "$KHEOPS_KEYCLOAK_USER" ]; then
    echo "Missing KHEOPS_KEYCLOAK_USER environment variable"
    missing_env_var_secret=true
fi
if [ -z "$KHEOPS_KEYCLOAK_REALMS" ]; then
    echo "Missing KHEOPS_KEYCLOAK_REALMS environment variable"
    missing_env_var_secret=true
fi


if [ "missing_env_var_secret" = true ]; then
    exit 1
fi


for f in ${SECRET_FILE_PATH}/*
do
  filename=$(basename "$f")
  value=$(cat ${f})
  sed -i "s|\${$filename}|$value|" ${REPLACE_FILE_PATH}
done


sed -i "s|\${kheops_postgresql_user}|$KHEOPS_AUTHDB_USER|" ${REPLACE_FILE_PATH}
sed -i "s|\${kheops_postgresql_url}|$KHEOPS_AUTHDB_URL/$KHEOPS_AUTHDB_NAME|" ${REPLACE_FILE_PATH}
sed -i "s|\${kheops_pacs_url}|http://$KHEOPS_PACS_PEP_HOST:$KHEOPS_PACS_PEP_PORT|" ${REPLACE_FILE_PATH}

sed -i "s|\${kheops_keycloak_uri}|$KHEOPS_KEYCLOAK_URI|" ${REPLACE_FILE_PATH}
sed -i "s|\${kheops_keycloak_clientid}|$KHEOPS_KEYCLOAK_CLIENTID|" ${REPLACE_FILE_PATH}
sed -i "s|\${kheops_keycloak_user}|$KHEOPS_KEYCLOAK_USER|" ${REPLACE_FILE_PATH}
sed -i "s|\${kheops_keycloak_realms}|$KHEOPS_KEYCLOAK_REALMS|" ${REPLACE_FILE_PATH}

catalina.sh run;
