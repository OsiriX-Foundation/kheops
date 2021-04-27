FROM openresty/openresty:1.19.3.1-3-alpine-fat

ENV NGINX_PREFIX /opt/openresty/nginx

WORKDIR $NGINX_PREFIX/

COPY lua $NGINX_PREFIX/lua/
COPY nginx_conf/nginx.conf /usr/local/openresty/nginx/conf/nginx.conf
COPY script.sh $NGINX_PREFIX/script.sh

CMD . $NGINX_PREFIX/script.sh
