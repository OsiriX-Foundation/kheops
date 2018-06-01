#! /bin/sh

for f in ${SECRET_FILE_PATH}
do
  filename=$(basename "$f")
  value=$(cat ${f})
  sed -i "s|\${$filename}|$value|" ${REPLACE_FILE_PATH}
done

sed -i "s|\${kheops_mysql_user}|$KHEOPS_MYSQL_USER|" ${REPLACE_FILE_PATH}
sed -i "s|\${kheops_mysql_url}|$KHEOPS_MYSQL_URL|" ${REPLACE_FILE_PATH}
sed -i "s|\${kheops_pacs_url}|$KHEOPS_PACS_URL|" ${REPLACE_FILE_PATH}

catalina.sh run;
