#! /bin/bash

env_check() {
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

env_check "KHEOPS_PACS_PEP_HOST" \
          "KHEOPS_PACS_PEP_PORT" \
          "KHEOPS_AUTHORIZATION_HOST" \
          "KHEOPS_AUTHORIZATION_PORT" \
          "KHEOPS_AUTHORIZATION_PATH" \
          "KHEOPS_ROOT_URL" \
          "KHEOPS_API_PATH" \
          "KHEOPS_CLIENT_DICOMWEBPROXYCLIENTID"


missing_secret=false

#Verify secrets
if ! [ -f /run/secrets/kheops_auth_hmasecret_post ]; then
    echo >&2 "Missing kheops_auth_hmasecret_post secret"
    missing_secret=true
fi
if ! [ -f /run/secrets/kheops_client_dicomwebproxysecret ]; then
    echo >&2 "Missing kheops_client_dicomwebproxysecret secret"
    missing_secret=true
fi

#if missing env var or secret => exit
if [ "$missing_secret" = true ]; then
    exit 1
fi

#get secrets and verify content
for f in /run/secrets/*
do
  filename=$(basename "$f")

  if [ "$filename" = "kubernetes.io" ]; then
    continue
  fi

  word_count=$(wc -w "$f" | cut -f1 -d" ")
  line_count=$(wc -l "$f" | cut -f1 -d" ")

  if [ "${word_count}" != 1 ] || [ "${line_count}" != 1 ]; then
    echo "Error with secret $filename. He contains $word_count word and $line_count line"
    exit 1
  fi
done

export CATALINA_OPTS="$CATALINA_OPTS -Donline.kheops.pacs.uri=\"http://$KHEOPS_PACS_PEP_HOST:$KHEOPS_PACS_PEP_PORT\""
export CATALINA_OPTS="$CATALINA_OPTS -Donline.kheops.auth_server.uri=\"http://$KHEOPS_AUTHORIZATION_HOST:$KHEOPS_AUTHORIZATION_PORT$KHEOPS_AUTHORIZATION_PATH\""
export CATALINA_OPTS="$CATALINA_OPTS -Donline.kheops.root.uri=\"$KHEOPS_ROOT_URL\""
export CATALINA_OPTS="$CATALINA_OPTS -Donline.kheops.client.dicomwebproxyclientid=\"$KHEOPS_CLIENT_DICOMWEBPROXYCLIENTID\""

HMAC_SECRET_POST="$(head -n 1 /run/secrets/kheops_auth_hmasecret_post)"
export HMAC_SECRET_POST

DICOMWEB_PROXY_SECRET="$(head -n 1 /run/secrets/kheops_client_dicomwebproxysecret)"
export DICOMWEB_PROXY_SECRET
