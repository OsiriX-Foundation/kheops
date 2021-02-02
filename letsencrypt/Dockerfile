FROM osirixfoundation/nginx-certbot:master

ENV SECRET_FILE_PATH=/run/secrets

COPY letsencrypt/kheops.conf /etc/nginx/conf.d/kheops.conf
COPY locations.conf /etc/nginx/kheops/01_locations.conf
COPY locations_secured_ui.conf /etc/nginx/kheops/03_locations_secured_ui.conf
COPY env_var_script.sh /docker-entrypoint.d/30-env_var_script.sh
COPY letsencrypt/root_url_script.sh /docker-entrypoint.d/89-root_url_script.sh
COPY nginx.conf /etc/nginx/nginx.conf
COPY nginx /etc/logrotate.d/nginx
COPY add_crontab.sh /docker-entrypoint.d/72-add_crontab.sh
COPY crontabfile /etc/crontabfile
COPY logcrontab /logcrontab

RUN chmod +x /docker-entrypoint.d/72-add_crontab.sh

RUN rm /var/log/nginx/access.log /var/log/nginx/error.log && \
    rm /etc/nginx/conf.d/default.conf
RUN apt-get install -y logrotate
RUN mv /etc/cron.daily/logrotate /etc/cron.hourly/logrotate

CMD ["nginx", "-g", "daemon off;"]