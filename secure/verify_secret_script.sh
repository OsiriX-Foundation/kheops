#!/bin/bash

missing_secret=false

#Verify secrets
if ! [ -f ${SECRET_FILE_PATH}/privkey.pem ]; then
    echo "Missing privkey.pem secret"
    missing_secret=true
else
   echo -e "secret privkey.pem \e[92mOK\e[0m"
fi
if ! [ -f ${SECRET_FILE_PATH}/fullchain.pem ]; then
    echo "Missing fullchain.pem secret"
    missing_secret=true
else
   echo -e "secret fullchain.pem \e[92mOK\e[0m"
fi

#if missing secret => exit
if [[ $missing_secret = true ]]; then
  exit 1
else
   echo -e "all nginx secrets \e[92mOK\e[0m"
fi