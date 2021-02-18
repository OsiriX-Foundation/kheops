#!/bin/bash

remplace_in_file() {
  chmod a+w $1

  sed -i "s|\${kibana_url}|http://$KIBANA_HOST:$KIBANA_PORT|" $1

}

missing_env_var_secret=false

#Verify environment variables

if [[ -z $KIBANA_HOST ]]; then
  echo "Missing KIBANA_HOST environment variable"
  missing_env_var_secret=true
else
   echo -e "environment variable KIBANA_HOST \e[92mOK\e[0m"
fi

if [[ -z $KIBANA_PORT ]]; then
  echo "Missing KIBANA_PORT environment variable"
  missing_env_var_secret=true
else
   echo -e "environment variable KIBANA_PORT \e[92mOK\e[0m"
fi


if [[ -f /run/secret/auth_basic ]]; then
  echo -e "secret auth_basic \e[92mOK\e[0m"
else
  echo "Missing auth_basic secret"
  missing_env_var_secret=true
fi

#if missing env var or secret => exit
if [[ $missing_env_var_secret = true ]]; then
  exit 1
else
   echo -e "kibana nginx env var and secret \e[92mOK\e[0m"
fi


if /usr/bin/find "/etc/nginx/kheops/" -mindepth 1 -maxdepth 1 -type f -print -quit 2>/dev/null | read v; then
    echo >&3 "$0: /etc/nginx/kheops/ is not empty, will attempt to perform configuration"

    echo >&3 "$0: Looking for .conf file in /etc/nginx/kheops/"
    find "/etc/nginx/kheops/" -follow -type f -print | sort -n | while read -r f; do
        case "$f" in
            *.conf)
                remplace_in_file ${f}
                ;;
            *) echo >&3 "$0: Ignoring $f";;
        esac
    done
fi

remplace_in_file /etc/nginx/conf.d/kheops.conf

echo "Ending setup kibana env var and secret"