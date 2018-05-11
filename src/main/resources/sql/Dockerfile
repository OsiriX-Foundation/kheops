FROM mysql/mysql-server:5.7

ENV MYSQL_ROOT_PASSWORD=mypwd

ADD create-mysql.sql docker-entrypoint-initdb.d
ADD create-toto-user.sql docker-entrypoint-initdb.d

