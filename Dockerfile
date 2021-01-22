FROM docker.elastic.co/beats/metricbeat:7.1.1

COPY metricbeat.yml /xxx/metricbeat.yml
COPY http.yml /xxx/http.yml

COPY entrypoint.sh /entrypoint.sh

USER root

RUN chmod 770 /usr/share/metricbeat
RUN chmod -R 777 /xxx 
RUN chmod +x /entrypoint.sh

USER metricbeat


ENTRYPOINT ["/entrypoint.sh"]
