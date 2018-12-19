#! /bin/sh

for f in ${SECRET_FILE_PATH}
do
  filename=$(basename "$f")
  value=$(cat ${f})
  sed -i "s|\${$filename}|$value|" ${REPLACE_FILE_PATH}
done

sed -i "s|\${kheops_postgresql_user}|$KHEOPS_AUTHDB_USER|" ${REPLACE_FILE_PATH}
sed -i "s|\${kheops_postgresql_url}|$KHEOPS_AUTHDB_URL/$KHEOPS_AUTHDB_NAME|" ${REPLACE_FILE_PATH}
sed -i "s|\${kheops_pacs_url}|http://$KHEOPS_PACS_PEP_HOST:$KHEOPS_PACS_PEP_PORT|" ${REPLACE_FILE_PATH}

catalina.sh run;
