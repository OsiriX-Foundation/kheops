#! /bin/sh

export CATALINA_OPTS="$CATALINA_OPTS -Donline.kheops.pacs.uri=\"http://$KHEOPS_PACS_PEP_HOST:$KHEOPS_PACS_PEP_PORT\""
export CATALINA_OPTS="$CATALINA_OPTS -Donline.kheops.auth_server.uri=\"http://$KHEOPS_AUTHORIZATION_HOST:$KHEOPS_AUTHORIZATION_PORT$KHEOPS_AUTHORIZATION_PATH\""
export CATALINA_OPTS="$CATALINA_OPTS -Donline.kheops.root.uri=\"$KHEOPS_ROOT_URL\""
export CATALINA_OPTS="$CATALINA_OPTS -Donline.kheops.client.dicomwebproxyclientid=\"$KHEOPS_CLIENT_DICOMWEBPROXYCLIENTID\""

kheops_auth_hmasecret_post="$(head -n 1 /run/secrets/kheops_auth_hmasecret_post)"
export CATALINA_OPTS="$CATALINA_OPTS -Donline.kheops.auth.hmacsecretpost=\"$kheops_auth_hmasecret_post\""

kheops_client_dicomwebproxysecret="$(head -n 1 /run/secrets/kheops_client_dicomwebproxysecret)"
export CATALINA_OPTS="$CATALINA_OPTS -Donline.kheops.client.dicomwebproxysecret=\"$kheops_client_dicomwebproxysecret\""
