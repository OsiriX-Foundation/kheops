#! /bin/sh

for f in ${SECRET_FILE_PATH}
do
  filename=$(basename "$f")
  value=$(cat ${f})
  echo $value
  echo $filename
  sed -i "s/\${$filename}/$value/" ${REPLACE_FILE_PATH}
done

catalina.sh run;


echo endscript
