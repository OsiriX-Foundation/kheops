FROM nginx:stable as builder

COPY . .

RUN apt-get update
RUN apt-get install -y curl

ENV METRICBEAT_VERSION 6.6.1
RUN curl -L -O https://artifacts.elastic.co/downloads/beats/metricbeat/metricbeat-${METRICBEAT_VERSION}-amd64.deb

ENV FILEBEAT_VERSION 6.6.1
RUN curl -L -O https://artifacts.elastic.co/downloads/beats/filebeat/filebeat-${FILEBEAT_VERSION}-amd64.deb
#########################################################################################
FROM nginx:stable

ENV SECRET_FILE_PATH=/run/secrets

COPY --from=builder kheops.conf /etc/nginx/conf.d/kheops.conf
COPY --from=builder metricbeat.conf /etc/nginx/conf.d/metricbeat.conf
COPY --from=builder script.sh /etc/nginx/conf.d/script.sh
COPY --from=builder nginx.conf /etc/nginx/nginx.conf

RUN chmod +x /etc/nginx/conf.d/script.sh


#METRICBEAT
ENV METRICBEAT_VERSION 6.6.1
COPY --from=builder metricbeat-${METRICBEAT_VERSION}-amd64.deb metricbeat-${METRICBEAT_VERSION}-amd64.deb
RUN dpkg -i metricbeat-${METRICBEAT_VERSION}-amd64.deb && \
    rm metricbeat-${METRICBEAT_VERSION}-amd64.deb
COPY --from=builder metricbeat.yml /etc/metricbeat/metricbeat.yml
COPY --from=builder metricbeat_nginx.yml /etc/metricbeat/modules.d/nginx.yml
RUN chmod go-w /etc/metricbeat/metricbeat.yml && \
    chmod go-w /etc/metricbeat/modules.d/nginx.yml


#FILEBEAT
ENV FILEBEAT_VERSION 6.6.1
COPY --from=builder filebeat-${FILEBEAT_VERSION}-amd64.deb filebeat-${FILEBEAT_VERSION}-amd64.deb
RUN dpkg -i filebeat-${FILEBEAT_VERSION}-amd64.deb && \
    rm filebeat-${FILEBEAT_VERSION}-amd64.deb
COPY --from=builder filebeat_nginx.yml /etc/filebeat/modules.d/nginx.yml
COPY --from=builder filebeat.yml /etc/filebeat/filebeat.yml
RUN chmod go-w /etc/filebeat/filebeat.yml &&  \
    chmod go-w /etc/filebeat/modules.d/nginx.yml




RUN rm /var/log/nginx/access.log /var/log/nginx/error.log

CMD ["./etc/nginx/conf.d/script.sh"]
