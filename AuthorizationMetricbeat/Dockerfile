FROM docker.elastic.co/beats/metricbeat:7.17.0

COPY metricbeat.yml /metricbeattmp/metricbeat.yml
COPY http.yml /metricbeattmp/http.yml

COPY entrypoint.sh /entrypoint.sh

USER root

RUN chmod 770 /usr/share/metricbeat
RUN chmod -R 777 /metricbeattmp 
RUN chmod +x /entrypoint.sh

USER metricbeat


ENTRYPOINT ["/entrypoint.sh"]
