#! /bin/sh

for f in ${SECRET_FILE_PATH}
do
  filename=$(basename "$f")
  value=$(cat ${f})
  sed -i "s|\${$filename}|$value|" ${REPLACE_FILE_PATH}
done

sed -i "s|\${kheops_pacs_url}|http://$KHEOPS_PACS_PEP_HOST:$KHEOPS_PACS_PEP_PORT|" ${REPLACE_FILE_PATH}
sed -i "s|\${kheops_authorization_url}|http://$KHEOPS_AUTHORIZATION_HOST:$KHEOPS_AUTHORIZATION_PORT$KHEOPS_AUTHORIZATION_PATH|" ${REPLACE_FILE_PATH}
sed -i "s|\${kheops_root_url}|$KHEOPS_ROOT_SCHEME://$KHEOPS_ROOT_HOST|" ${REPLACE_FILE_PATH}

catalina.sh run;
