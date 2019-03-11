FROM nginx:stable

ENV SECRET_FILE_PATH=/run/secrets

COPY kheops.conf /etc/nginx/conf.d/kheops.conf
COPY metricbeat.conf /etc/nginx/conf.d/metricbeat.conf
COPY script.sh /etc/nginx/conf.d/script.sh
COPY nginx.conf /etc/nginx/nginx.conf

RUN chmod +x /etc/nginx/conf.d/script.sh

RUN apt-get update
RUN apt-get install -y curl

#METRICBEAT
ENV METRICBEAT_VERSION 6.6.1
RUN curl -L -O https://artifacts.elastic.co/downloads/beats/metricbeat/metricbeat-${METRICBEAT_VERSION}-amd64.deb
RUN dpkg -i metricbeat-${METRICBEAT_VERSION}-amd64.deb
RUN rm metricbeat-${METRICBEAT_VERSION}-amd64.deb

COPY metricbeat.yml /etc/metricbeat/metricbeat.yml
RUN chmod go-w /etc/metricbeat/metricbeat.yml

#RUN metricbeat modules enable nginx
COPY metricbeat_nginx.yml /etc/metricbeat/modules.d/nginx.yml
RUN chmod go-w /etc/metricbeat/modules.d/nginx.yml


#FILEBEAT
ENV FILEBEAT_VERSION 6.6.1
RUN curl -L -O https://artifacts.elastic.co/downloads/beats/filebeat/filebeat-${FILEBEAT_VERSION}-amd64.deb
RUN dpkg -i filebeat-${FILEBEAT_VERSION}-amd64.deb
RUN rm filebeat-${FILEBEAT_VERSION}-amd64.deb

COPY filebeat.yml /etc/filebeat/filebeat.yml
RUN chmod go-w /etc/filebeat/filebeat.yml

#RUN filebeat modules enable nginx
COPY filebeat_nginx.yml /etc/filebeat/modules.d/nginx.yml
RUN chmod go-w /etc/filebeat/modules.d/nginx.yml


RUN apt-get remove -y curl

RUN rm /var/log/nginx/access.log /var/log/nginx/error.log

CMD ["./etc/nginx/conf.d/script.sh"]
