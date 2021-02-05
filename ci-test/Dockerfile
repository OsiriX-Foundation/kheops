FROM nginx:stable

ENV SECRET_FILE_PATH=/run/secrets

RUN mkdir /etc/nginx/kheops

COPY nginx.conf /etc/nginx/nginx.conf
COPY ci-test/kheops.conf /etc/nginx/conf.d/kheops.conf
COPY locations.conf /etc/nginx/kheops/01_locations.conf
COPY ci-test/dev_location.conf /etc/nginx/kheops/02_dev_location.conf
COPY locations_link_unsecured.conf /etc/nginx/kheops/03_locations_link_unsecured.conf
COPY locations_unsecured_ui.conf /etc/nginx/kheops/04_locations_unsecured_ui.conf
COPY env_var_script.sh /docker-entrypoint.d/30-env_var_script.sh

RUN rm /var/log/nginx/access.log /var/log/nginx/error.log && \
    rm /etc/nginx/conf.d/default.conf

CMD ["nginx", "-g", "daemon off;"]