FROM mysql/mysql-server:5.7

LABEL org.label-schema.vcs-ref='git rev-parse --short HEAD' \
      org.label-schema.vcs-url="https://github.com/OsiriX-Foundation/KheopsAuthorization"

ADD create-mysql.sql docker-entrypoint-initdb.d
ADD ep.sh docker-entrypoint-initdb.d
