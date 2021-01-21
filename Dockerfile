FROM alpine:3.13.0

COPY entrypoint.sh /entrypoint.sh

#METRICBEAT
COPY --from=osirixfoundation/kheops-beat:latest /install/deb/metricbeat-amd64.deb .
RUN dpkg -i metricbeat-amd64.deb && \
 rm metricbeat-amd64.deb && \
 rm /etc/metricbeat/metricbeat.reference.yml && \
 rm /etc/metricbeat/modules.d/*

COPY metricbeat.yml /etc/metricbeat/metricbeat.yml
COPY http.yml /etc/metricbeat/modules.d/http.yml
RUN chmod go-w /etc/metricbeat/metricbeat.yml
RUN chmod go-w /etc/metricbeat/modules.d/http.yml


ENTRYPOINT ["/entrypoint.sh"]
