FROM nginx:stable

COPY kheops.conf /etc/nginx/conf.d/kheops.conf
COPY script.sh /etc/nginx/conf.d/script.sh

RUN chmod +x /etc/nginx/conf.d/script.sh

RUN apt-get update
RUN apt-get install -y curl
RUN curl -L -O https://artifacts.elastic.co/downloads/beats/metricbeat/metricbeat-6.6.0-amd64.deb
RUN dpkg -i metricbeat-6.6.0-amd64.deb

COPY metricbeat.yml /etc/metricbeat/metricbeat.yml

RUN metricbeat modules enable nginx
RUN metricbeat modules list
RUN metricbeat setup
RUN service metricbeat start




CMD ["./etc/nginx/conf.d/script.sh"]
