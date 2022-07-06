#! /bin/bash

check_env() {
  local missing_env_var=false

  while [[ -n $1 ]]; do
    local var="$1"
    if [[ -z ${!var} ]]; then
     echo >&2 "Missing $var environment variable"
     missing_env_var=true
    fi
    shift
  done
  if [[ $missing_env_var = true ]]; then
    exit 1
  fi
}

check_secrets() {
  local missing_secret=false;

    while [[ -n $1 ]]; do
    local var="$1"
    if [[ -f $var ]]; then
        word_count=$(wc -w "$var" | cut -f1 -d" ")
        line_count=$(wc -l "$var" | cut -f1 -d" ")

        if [[ "${word_count}" != 1 ]] || [[ "${line_count}" != 1 ]]; then
          echo "Error with secret $var. It contains $word_count words and $line_count lines"
          missing_secret=true
        fi
    else
      echo >&2 "Missing $var secret"
      missing_secret=true
    fi
    shift

    echo "Finished checking secrets"
  done
#  if [[ $missing_secret = true ]]; then
#    exit 1
#  fi
}

check_env "KHEOPS_AUTHDB_USER" \
          "KHEOPS_AUTHDB_URL" \
          "KHEOPS_AUTHDB_NAME" \
          "KHEOPS_ROOT_URL" \
          "KHEOPS_PACS_PEP_HOST" \
          "KHEOPS_PACS_PEP_PORT" \
          "KHEOPS_OIDC_PROVIDER" \
          "KHEOPS_CLIENT_DICOMWEBPROXYCLIENTID" \
          "KHEOPS_CLIENT_ZIPPERCLIENTID"

check_secrets "/run/secrets/kheops_authdb_pass" \
              "/run/secrets/kheops_auth_hmasecret" \
              "/run/secrets/kheops_client_dicomwebproxysecret" \
              "/run/secrets/kheops_client_zippersecret"

if [ -z "$KHEOPS_WELCOMEBOT_WEBHOOK" ]; then
    echo "No KHEOPS_WELCOMEBOT_WEBHOOK environment variable, welcomebot is disabled"
fi

if [ -z "$KHEOPS_OAUTH_SCOPE" ]; then
    echo "$KHEOPS_OAUTH_SCOPE not set, not requiring any scopes on access_tokens"
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
  echo $value
  sed -i "s|\${$filename}|$value|" ${REPLACE_FILE_PATH}
done


#get env var

sed -i "s|\${kheops_root_url}|$KHEOPS_ROOT_URL|" ${REPLACE_FILE_PATH}
sed -i "s|\${kheops_postgresql_user}|$KHEOPS_AUTHDB_USER|" ${REPLACE_FILE_PATH}
sed -i "s|\${kheops_postgresql_url}|$KHEOPS_AUTHDB_URL/$KHEOPS_AUTHDB_NAME|" ${REPLACE_FILE_PATH}
sed -i "s|\${kheops_pacs_url}|http://$KHEOPS_PACS_PEP_HOST:$KHEOPS_PACS_PEP_PORT|" ${REPLACE_FILE_PATH}

sed -i "s|\${kheops_client_dicomwebproxyclientid}|$KHEOPS_CLIENT_DICOMWEBPROXYCLIENTID|" ${REPLACE_FILE_PATH}
sed -i "s|\${kheops_client_zipperclientid}|$KHEOPS_CLIENT_ZIPPERCLIENTID|" ${REPLACE_FILE_PATH}
sed -i "s|\${kheops_oidc_provider}|$KHEOPS_AUTH_OIDC_PROVIDER|" ${REPLACE_FILE_PATH}
sed -i "s|\${kheops_oauth_scope}|$KHEOPS_OAUTH_SCOPE|" ${REPLACE_FILE_PATH}
sed -i "s|\${kheops_welcomebot_webhook}|$KHEOPS_WELCOMEBOT_WEBHOOK|" ${REPLACE_FILE_PATH}

export UMASK=022

echo "Ending setup secrets and env var"
