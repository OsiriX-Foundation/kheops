FROM nginx:stable

COPY kheops.conf /etc/nginx/conf.d/kheops.conf
COPY script.sh /etc/nginx/conf.d/script.sh
COPY metricbeat.yml /etc/metricbeat/metricbeat.yml

RUN chmod +x /etc/nginx/conf.d/script.sh

RUN curl -L -O https://artifacts.elastic.co/downloads/beats/metricbeat/metricbeat-6.6.0-amd64.deb
RUN sudo dpkg -i metricbeat-6.6.0-amd64.deb
RUN sudo metricbeat modules enable nginx
RUN sudo metricbeat setup
RUN sudo service metricbeat start




CMD ["./etc/nginx/conf.d/script.sh"]
