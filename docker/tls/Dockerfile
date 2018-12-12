FROM nginx:stable

ENV SERVER_NAME=localhost

RUN rm /etc/nginx/conf.d/default.conf

COPY dist /usr/share/nginx/html
ADD assets /usr/share/nginx/html/src

COPY ui.conf /etc/nginx/conf.d/ui.conf
COPY script.sh /etc/nginx/conf.d/script.sh

RUN chmod +x /etc/nginx/conf.d/script.sh

CMD ["./etc/nginx/conf.d/script.sh"]
