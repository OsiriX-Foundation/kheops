#! /bin/sh

missing_env_var=false

#Verify environment variables
if [ -z "$KHEOPS_AUTHORIZATION_HOST" ]; then
    echo "Missing KHEOPS_AUTHORIZATION_HOST environment variable"
    missing_env_var=true
fi
if [ -z "$KHEOPS_AUTHORIZATION_PORT" ]; then
    echo "Missing KHEOPS_AUTHORIZATION_PORT environment variable"
    missing_env_var=true
fi
if [ -z "$KHEOPS_AUTHORIZATION_PATH" ]; then
    echo "Missing KHEOPS_AUTHORIZATION_PATH environment variable"
    missing_env_var=true
fi
if [ -z "$KHEOPS_PACS_PEP_HOST" ]; then
    echo "Missing KHEOPS_PACS_PEP_HOST environment variable"
    missing_env_var=true
fi
if [ -z "$KHEOPS_PACS_PEP_PORT" ]; then
    echo "Missing KHEOPS_PACS_PEP_PORT environment variable"
    missing_env_var=true
fi

#if missing env var => exit
if [ "$missing_env_var" = true ]; then
    exit 1
fi

sed -i "s|\${kheops_auth_url}|http://$KHEOPS_AUTHORIZATION_HOST:$KHEOPS_AUTHORIZATION_PORT$KHEOPS_AUTHORIZATION_PATH|" ${REPLACE_FILE_PATH}
sed -i "s|\${kheops_pacs_url}|http://$KHEOPS_PACS_PEP_HOST:$KHEOPS_PACS_PEP_PORT|" ${REPLACE_FILE_PATH}

catalina.sh run;
