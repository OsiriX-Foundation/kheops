#! /bin/sh

echo setenvsetenvsetenv

if [ -f /run/secrets/kheops_superuser_hmasecret ]; then
    filename="/run/secrets/kheops_superuser_hmasecret"
    superuser_hmasecret=$(head -n 1 $filename)
else
    superuser_hmasecret=XXX
fi


export KHEOPS_SUPERUSER_HMASECRET=$superuser_hmasecret
#export KHEOPS_AUTH_HMASECRET=$auth_hmasecret
#export KHEOPS_JDBC_USER=$jdbc_user
#export KHEOPS_JDBC_PASSWORD=$jdbc_user
