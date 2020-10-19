FROM nginx:stable

ENV SECRET_FILE_PATH=/run/secrets

COPY secure/kheops.conf /etc/nginx/conf.d/kheops.conf
COPY locations.conf /etc/nginx/kheops/locations.conf
COPY env_var_script.sh /docker-entrypoint.d/30-env_var_script.sh
COPY secure/verify_secret_script.sh /docker-entrypoint.d/30-verify_secret_script.sh
COPY filebeat_script.sh /docker-entrypoint.d/30-filebeat_script.sh
COPY nginx.conf /etc/nginx/nginx.conf
COPY chain.pem /etc/nginx/chain.pem


#FILEBEAT
COPY --from=osirixfoundation/kheops-beat:latest /install/deb/filebeat-amd64.deb .
RUN dpkg -i filebeat-amd64.deb && \
    rm filebeat-amd64.deb
COPY filebeat.yml /etc/filebeat/filebeat.yml
RUN chmod go-w /etc/filebeat/filebeat.yml

RUN rm /var/log/nginx/access.log /var/log/nginx/error.log && \
    rm /etc/nginx/conf.d/default.conf

RUN chmod +x /docker-entrypoint.d/30-env_var_script.sh
RUN chmod +x /docker-entrypoint.d/30-verify_secret_script.sh
RUN chmod +x /docker-entrypoint.d/30-filebeat_script.sh

CMD ["nginx -g 'daemon off;'"]