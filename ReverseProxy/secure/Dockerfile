FROM nginx:1.21.6

ENV SECRET_FILE_PATH=/run/secrets

COPY secure/kheops.conf /etc/nginx/conf.d/kheops.conf
COPY locations.conf /etc/nginx/kheops/01_locations.conf
COPY location_link_secured.conf /etc/nginx/kheops/03_location_link_secured.conf
COPY location_ui.conf /etc/nginx/kheops/04_location_ui.conf
COPY env_var_script.sh /docker-entrypoint.d/30-env_var_script.sh
COPY secure/verify_secret_script.sh /docker-entrypoint.d/30-verify_secret_script.sh
COPY nginx.conf /etc/nginx/nginx.conf
COPY chain.pem /etc/nginx/chain.pem

COPY add_crontab.sh /docker-entrypoint.d/72-add_crontab.sh
COPY logcrontab.sh /logcrontab.sh
COPY nginxday /etc/logrotate.d/nginxday
COPY nginxsize /etc/logrotate.d/nginxsize

RUN rm /var/log/nginx/access.log /var/log/nginx/error.log && \
    rm /etc/nginx/conf.d/default.conf
    
RUN apt-get update && apt-get install -y logrotate

CMD ["nginx", "-g", "daemon off;"]
