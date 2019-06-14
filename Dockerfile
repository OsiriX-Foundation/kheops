FROM nginx:stable

ENV SECRET_FILE_PATH=/run/secrets

COPY kheops.conf /etc/nginx/conf.d/kheops.conf
COPY script.sh /etc/nginx/conf.d/script.sh
COPY nginx.conf /etc/nginx/nginx.conf
COPY chain.pem /etc/nginx/chain.pem

RUN chmod +x /etc/nginx/conf.d/script.sh

#FILEBEAT
COPY --from=osirixfoundation/kheops-beat:latest /install/deb/filebeat-amd64.deb .
RUN dpkg -i filebeat-amd64.deb && \
    rm filebeat-amd64.deb
COPY filebeat.yml /etc/filebeat/filebeat.yml
RUN chmod go-w /etc/filebeat/filebeat.yml


RUN rm /var/log/nginx/access.log /var/log/nginx/error.log && \
    rm /etc/nginx/conf.d/default.conf

CMD ["./etc/nginx/conf.d/script.sh"]
