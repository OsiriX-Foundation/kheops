FROM nginx:stable

COPY kheops.conf /etc/nginx/conf.d/kheops.conf
COPY script.sh /etc/nginx/conf.d/script.sh

CMD ["./etc/nginx/conf.d/script.sh"]
