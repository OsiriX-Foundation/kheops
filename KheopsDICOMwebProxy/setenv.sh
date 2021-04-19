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
  done
  if [[ $missing_secret = true ]]; then
    exit 1
  fi
}

check_env "KHEOPS_PACS_PEP_HOST" \
          "KHEOPS_PACS_PEP_PORT" \
          "KHEOPS_AUTHORIZATION_HOST" \
          "KHEOPS_AUTHORIZATION_PORT" \
          "KHEOPS_AUTHORIZATION_PATH" \
          "KHEOPS_ROOT_URL" \
          "KHEOPS_API_PATH" \
          "KHEOPS_CLIENT_DICOMWEBPROXYCLIENTID"

check_secrets "/run/secrets/kheops_auth_hmasecret_post" \
              "/run/secrets/kheops_client_dicomwebproxysecret"

export CATALINA_OPTS="$CATALINA_OPTS -Donline.kheops.pacs.uri=\"http://$KHEOPS_PACS_PEP_HOST:$KHEOPS_PACS_PEP_PORT\""
export CATALINA_OPTS="$CATALINA_OPTS -Donline.kheops.auth_server.uri=\"http://$KHEOPS_AUTHORIZATION_HOST:$KHEOPS_AUTHORIZATION_PORT$KHEOPS_AUTHORIZATION_PATH\""
export CATALINA_OPTS="$CATALINA_OPTS -Donline.kheops.root.uri=\"$KHEOPS_ROOT_URL\""
export CATALINA_OPTS="$CATALINA_OPTS -Donline.kheops.client.dicomwebproxyclientid=\"$KHEOPS_CLIENT_DICOMWEBPROXYCLIENTID\""

HMAC_SECRET_POST="$(head -n 1 /run/secrets/kheops_auth_hmasecret_post)"
export HMAC_SECRET_POST

DICOMWEB_PROXY_SECRET="$(head -n 1 /run/secrets/kheops_client_dicomwebproxysecret)"
export DICOMWEB_PROXY_SECRET
