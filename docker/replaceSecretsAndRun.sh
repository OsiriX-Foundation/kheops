#! /bin/sh


if ! [ -f ${SECRET_FILE_PATH}/kheops_authdb_pass ]; then
    echo "Missing kheops_authdb_pass secret"
    exit 1
fi
if ! [ -f ${SECRET_FILE_PATH}/kheops_superuser_hmasecret ]; then
    echo "Missing kheops_superuser_hmasecret secret"
    exit 1
fi
if ! [ -f ${SECRET_FILE_PATH}/kheops_auth_hmasecret ]; then
    echo "Missing kheops kheops_auth_hmasecret secret"
    exit 1
fi
if ! [ -f ${SECRET_FILE_PATH}/kheops_keycloak_clientsecret ]; then
    echo "Missing kheops_keycloak_clientsecret secret"
    exit 1
fi
if ! [ -f ${SECRET_FILE_PATH}/kheops_keycloak_password ]; then
    echo "Missing kheops_keycloak_password secret"
    exit 1
fi


for f in ${SECRET_FILE_PATH}
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
