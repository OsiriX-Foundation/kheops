#!/bin/bash

if [ -f /run/secrets/mysql_user ]; then
    filename="/run/secrets/mysql_user"
    user=$(head -n 1 $filename)
else
    user=default_user
fi

if [ -f /run/secrets/mysql_password ]; then
    filename="/run/secrets/mysql_password"
    pwd=$(head -n 1 $filename)
else
    pwd=default_pwd
fi

if [ -f /run/secrets/mysql_root_password ]; then
    filename="/run/secrets/mysql_root_password"
    rootpwd=$(head -n 1 $filename)
else
    rootpwd=rootpwd
fi


mysql -uroot -p$rootpwd<<MYSQL_SCRIPT

CREATE USER '$user'@'%' IDENTIFIED BY '$pwd';
GRANT ALL ON kheops.* TO '$user'@'%';

MYSQL_SCRIPT
