FROM openresty/openresty:1.19.9.1-5-alpine

ENV NGINX_PREFIX /opt/openresty/nginx

WORKDIR $NGINX_PREFIX/

COPY lua $NGINX_PREFIX/lua/
COPY nginx_conf/nginx.conf /usr/local/openresty/nginx/conf/nginx.conf
COPY script.sh $NGINX_PREFIX/script.sh

RUN mkdir /var/log/nginx

CMD . $NGINX_PREFIX/script.sh
