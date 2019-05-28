FROM nginx:stable as builder

COPY . .

ENV FILEBEAT_VERSION 6.7.1

RUN apt-get update && \
    apt-get install -y curl && \
    curl -L -O https://artifacts.elastic.co/downloads/beats/filebeat/filebeat-${FILEBEAT_VERSION}-amd64.deb
#########################################################################################
FROM nginx:stable

ENV SECRET_FILE_PATH=/run/secrets

COPY --from=builder kheops.conf /etc/nginx/conf.d/kheops.conf
COPY --from=builder script.sh /etc/nginx/conf.d/script.sh
COPY --from=builder nginx.conf /etc/nginx/nginx.conf
COPY --from=builder chain.pem /etc/nginx/chain.pem

RUN chmod +x /etc/nginx/conf.d/script.sh

#FILEBEAT
ENV FILEBEAT_VERSION 6.7.1
COPY --from=builder filebeat-${FILEBEAT_VERSION}-amd64.deb filebeat-${FILEBEAT_VERSION}-amd64.deb
RUN dpkg -i filebeat-${FILEBEAT_VERSION}-amd64.deb && \
    rm filebeat-${FILEBEAT_VERSION}-amd64.deb
COPY --from=builder filebeat.yml /etc/filebeat/filebeat.yml
RUN chmod go-w /etc/filebeat/filebeat.yml


RUN rm /var/log/nginx/access.log /var/log/nginx/error.log && \
    rm /etc/nginx/conf.d/default.conf

CMD ["./etc/nginx/conf.d/script.sh"]
