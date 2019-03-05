FROM nginx:stable

COPY kheops.conf /etc/nginx/conf.d/kheops.conf
COPY script.sh /etc/nginx/conf.d/script.sh

RUN chmod +x /etc/nginx/conf.d/script.sh


RUN apt-get update
RUN apt-get install -y curl

#METRICBEAT
RUN curl -L -O https://artifacts.elastic.co/downloads/beats/metricbeat/metricbeat-6.6.0-amd64.deb
RUN dpkg -i metricbeat-6.6.0-amd64.deb
RUN rm metricbeat-6.6.0-amd64.deb

COPY metricbeat.yml /etc/metricbeat/metricbeat.yml

RUN metricbeat modules list
RUN metricbeat modules enable nginx
RUN metricbeat setup
RUN service metricbeat start

#FILEBEAT
RUN curl -L -O https://artifacts.elastic.co/downloads/beats/filebeat/filebeat-6.6.0-amd64.deb
RUN dpkg -i filebeat-6.6.0-amd64.deb
RUN rm filebeat-6.6.0-amd64.deb

COPY filebeat.yml /etc/filebeat/filebeat.yml

RUN filebeat modules enable nginx
RUN filebeat setup
RUN service filebeat start

RUN apt-get remove -y curl


RUN rm /var/log/nginx/access.log /var/log/nginx/error.log
CMD ["./etc/nginx/conf.d/script.sh"]
