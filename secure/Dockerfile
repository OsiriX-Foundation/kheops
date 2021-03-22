FROM nginx:1.19.8

ENV SECRET_FILE_PATH=/run/secrets

COPY secure/kheops.conf /etc/nginx/conf.d/kheops.conf
COPY locations.conf /etc/nginx/kheops/01_locations.conf
COPY location_link_secured.conf /etc/nginx/kheops/03_location_link_secured.conf
COPY location_secured_ui.conf /etc/nginx/kheops/04_location_secured_ui.conf
COPY env_var_script.sh /docker-entrypoint.d/30-env_var_script.sh
COPY secure/verify_secret_script.sh /docker-entrypoint.d/30-verify_secret_script.sh
COPY nginx.conf /etc/nginx/nginx.conf
COPY chain.pem /etc/nginx/chain.pem

RUN rm /var/log/nginx/access.log /var/log/nginx/error.log && \
    rm /etc/nginx/conf.d/default.conf

CMD ["nginx", "-g", "daemon off;"]