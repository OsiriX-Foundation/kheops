FROM nginx:1.21.6

ENV SECRET_FILE_PATH=/run/secrets

COPY nginx.conf /etc/nginx/nginx.conf
COPY ci-test/kheops.conf /etc/nginx/conf.d/kheops.conf
COPY locations.conf /etc/nginx/kheops/01_locations.conf
COPY ci-test/dev_location.conf /etc/nginx/kheops/02_dev_location.conf
COPY location_link_unsecured.conf /etc/nginx/kheops/03_location_link_unsecured.conf
COPY location_ui.conf /etc/nginx/kheops/04_location_ui.conf
COPY env_var_script.sh /docker-entrypoint.d/30-env_var_script.sh

RUN rm /var/log/nginx/access.log /var/log/nginx/error.log && \
    rm /etc/nginx/conf.d/default.conf

CMD ["nginx", "-g", "daemon off;"]
