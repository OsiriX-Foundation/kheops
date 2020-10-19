FROM nginx:stable

ENV SECRET_FILE_PATH=/run/secrets

RUN mkdir /etc/nginx/kheops

COPY ci-test/kheops.conf /etc/nginx/conf.d/kheops.conf
COPY locations.conf /etc/nginx/kheops/locations.conf
COPY ci-test/dev_location.conf /etc/nginx/kheops/dev_location.conf
COPY env_var_script.sh /docker-entrypoint.d/30-env_var_script.sh
COPY start_nginx_script.sh /docker-entrypoint.d/60-start_nginx_script.sh

RUN chmod +x /docker-entrypoint.d/30-env_var_script.sh
RUN chmod +x /docker-entrypoint.d/60-start_nginx_script.sh