#! /bin/sh

sed -i "s|\${kheops_auth_url}|$KHEOPS_AUTHORIZATION_URL|" ${REPLACE_FILE_PATH}
sed -i "s|\${kheops_pacs_url}|$KHEOPS_PACS_URL|" ${REPLACE_FILE_PATH}

catalina.sh run;
