FROM nginx:stable

ENV SECRET_FILE_PATH=/run/secrets

RUN mkdir /etc/nginx/kheops

COPY nginx.conf /etc/nginx/nginx.conf
COPY ci-test/kheops.conf /etc/nginx/conf.d/kheops.conf
COPY locations.conf /etc/nginx/kheops/locations.conf
COPY ci-test/dev_location.conf /etc/nginx/kheops/dev_location.conf
COPY env_var_script.sh /docker-entrypoint.d/30-env_var_script.sh

CMD ["nginx", "-g", "daemon off;"]