#! /bin/sh

for f in ${SECRET_FILE_PATH}
do
  filename=$(basename "$f")
  value=$(cat ${f})
  sed -i "s|\${$filename}|$value|" ${REPLACE_FILE_PATH}
done

sed -i "s|\${kheops_postgresql_user}|$KHEOPS_POSTGRESQL_USER|" ${REPLACE_FILE_PATH}
sed -i "s|\${kheops_postgresql_url}|$KHEOPS_POSTGRESQL_URL|" ${REPLACE_FILE_PATH}
sed -i "s|\${kheops_pacs_url}|$KHEOPS_PACS_URL|" ${REPLACE_FILE_PATH}

catalina.sh run;
