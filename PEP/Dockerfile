FROM openresty/openresty:1.19.3.1-3-alpine-fat

ENV NGINX_PREFIX /opt/openresty/nginx
#  /etc/nginx/conf.d/*.conf

WORKDIR $NGINX_PREFIX/

#RUN rm -rf conf/* html/*
#RUN rm /etc/nginx/conf.d/default.conf
COPY hosts/proxy/default/nginx/lua /opt/openresty/nginx/lua/
COPY hosts/proxy/default/nginx/conf/nginx.conf /usr/local/openresty/nginx/conf/nginx.conf
COPY hosts/proxy/default/nginx/script.sh $NGINX_PREFIX/script.sh


#RUN chmod +x $NGINX_PREFIX/script.sh

CMD . $NGINX_PREFIX/script.sh
