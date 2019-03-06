FROM nginx:stable

ENV SECRET_FILE_PATH=/run/secrets

COPY kheops.conf /etc/nginx/conf.d/kheops.conf
COPY metricbeat.conf /etc/nginx/conf.d/metricbeat.conf
COPY script.sh /etc/nginx/conf.d/script.sh

RUN chmod +x /etc/nginx/conf.d/script.sh


RUN apt-get update
RUN apt-get install -y curl

#METRICBEAT
RUN curl -L -O https://artifacts.elastic.co/downloads/beats/metricbeat/metricbeat-6.6.0-amd64.deb
RUN dpkg -i metricbeat-6.6.0-amd64.deb
RUN rm metricbeat-6.6.0-amd64.deb

COPY metricbeat.yml /etc/metricbeat/metricbeat.yml
RUN chmod go-w /etc/metricbeat/metricbeat.yml

#RUN metricbeat modules list
RUN metricbeat modules enable nginx
#RUN metricbeat modules list
COPY nginx.yml /etc/metricbeat/modules.d/nginx.yml
RUN chmod go-w /etc/metricbeat/modules.d/nginx.yml
#RUN metricbeat setup
#RUN cat /etc/metricbeat/metricbeat.yml


#FILEBEAT
RUN curl -L -O https://artifacts.elastic.co/downloads/beats/filebeat/filebeat-6.6.0-amd64.deb
RUN dpkg -i filebeat-6.6.0-amd64.deb
RUN rm filebeat-6.6.0-amd64.deb

COPY filebeat.yml /etc/filebeat/filebeat.yml
RUN chmod go-w /etc/filebeat/filebeat.yml

RUN filebeat modules enable nginx
COPY filebeat_nginx.yml /etc/filebeat/modules.d/nginx.yml
RUN chmod go-w /etc/filebeat/modules.d/nginx.yml
#RUN filebeat setup

RUN apt-get remove -y curl


RUN rm /var/log/nginx/access.log /var/log/nginx/error.log

CMD ["./etc/nginx/conf.d/script.sh"]
