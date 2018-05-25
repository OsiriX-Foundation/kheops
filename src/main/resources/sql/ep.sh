#!/bin/bash

user=$KHEOPS_MYSQL_USER

if [ -f /run/secrets/mysql_password ]; then
    filename="/run/secrets/mysql_password"
    pwd=$(head -n 1 $filename)
else
    echo "Missing kheops mysql_password secret"
    pwd=default_pwd
fi

if [ -f /run/secrets/mysql_root_password ]; then
    filename="/run/secrets/mysql_root_password"
    rootpwd=$(head -n 1 $filename)
else
    echo "Missing kheops mysql_root_password secret"
    rootpwd=rootpwd
fi


mysql -uroot -p$rootpwd<<MYSQL_SCRIPT

CREATE USER '$user'@'%' IDENTIFIED BY '$pwd';
GRANT ALL ON kheops.* TO '$user'@'%';

MYSQL_SCRIPT
