#!/bin/bash
filename="/run/secrets/my_mysql_user"
user=$(head -n 1 $filename)

filename="/run/secrets/my_mysql_password"
pwd=$(head -n 1 $filename)

filename="/run/secrets/mysql_root_password"
root_pwd=$(head -n 1 $filename)

mysql -uroot -p$rootpwd<<MYSQL_SCRIPT

CREATE USER '$user'@'%' IDENTIFIED BY '$pwd';
GRANT ALL ON *.* TO '$user'@'%';

MYSQL_SCRIPT
