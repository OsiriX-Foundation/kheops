#! /bin/sh

for f in ${SECRET_FILE_PATH}
do
  filename=$(basename "$f")
  value=$(cat ${f})
  sed -i "s|\${$filename}|$value|" ${REPLACE_FILE_PATH}
done

sed -i "s|\${kheops_pacs_url}|$KHEOPS_PACS_URL|" ${REPLACE_FILE_PATH}
sed -i "s|\${kheops_authorization_url}|$KHEOPS_AUTHORIZATION_URL|" ${REPLACE_FILE_PATH}
sed -i "s|\${kheops_root_url}|$KHEOPS_ROOT_URL|" ${REPLACE_FILE_PATH}

catalina.sh run;
