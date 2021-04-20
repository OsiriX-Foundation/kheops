#! /bin/sh

missing_env_var=false

#Verify environment variables
if [ -z "$KHEOPS_DICOMWEB_PROXY_HOST" ]; then
    echo "Missing KHEOPS_DICOMWEB_PROXY_HOST environment variable"
    missing_env_var=true
fi
if [ -z "$KHEOPS_DICOMWEB_PROXY_PORT" ]; then
    echo "Missing KHEOPS_DICOMWEB_PROXY_PORT environment variable"
    missing_env_var=true
fi

#if missing env var => exit
if [ "$missing_env_var" = true ]; then
    exit 1
fi

#get secrets and verify content
for f in ${SECRET_FILE_PATH}/*
do
  filename=$(basename "$f")
  
  if [ "$filename" = "kubernetes.io" ]; then
    continue
  fi
  
  word_count=$(wc -w $f | cut -f1 -d" ")
  line_count=$(wc -l $f | cut -f1 -d" ")

  if [ ${word_count} != 1 ] || [ ${line_count} != 1 ]; then
    echo Error with secret $filename. He contains $word_count word and $line_count line
    exit 1
  fi

  value=$(cat ${f})
  sed -i "s|\${$filename}|$value|" ${REPLACE_FILE_PATH}
done

sed -i "s|\${kheops_dicomwebproxy_url}|http://$KHEOPS_DICOMWEB_PROXY_HOST:$KHEOPS_DICOMWEB_PROXY_PORT|" ${REPLACE_FILE_PATH}

#######################################################################################
#ELASTIC SEARCH

if ! [ -z "$KHEOPS_ZIPPER_ENABLE_ELASTIC" ]; then
    if [ "$KHEOPS_ZIPPER_ENABLE_ELASTIC" = true ]; then

        echo "Start init filebeat"
        missing_env_var_secret=false

        if [[ -z $KHEOPS_ZIPPER_ELASTIC_INSTANCE ]]; then
          echo "Missing KHEOPS_ZIPPER_ELASTIC_INSTANCE environment variable"
          missing_env_var_secret=true
        else
           echo -e "environment variable KHEOPS_ZIPPER_ELASTIC_INSTANCE \e[92mOK\e[0m"
           sed -i "s|\${instance}|$KHEOPS_ZIPPER_ELASTIC_INSTANCE|" /etc/filebeat/filebeat.yml
        fi

        if [[ -z $KHEOPS_ZIPPER_LOGSTASH_URL ]]; then
          echo "Missing KHEOPS_ZIPPER_LOGSTASH_URL environment variable"
          missing_env_var_secret=true
        else
           echo -e "environment variable KHEOPS_ZIPPER_LOGSTASH_URL \e[92mOK\e[0m"
           sed -i "s|\${logstash_url}|$KHEOPS_ZIPPER_LOGSTASH_URL|" /etc/filebeat/filebeat.yml
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
    echo "[INFO] : Missing KHEOPS_ZIPPER_ENABLE_ELASTIC environment variable. Elastic is not enable."
fi

#######################################################################################

catalina.sh run;
