FROM alpine:3.13.0

COPY entrypoint.sh /entrypoint.sh

#METRICBEAT
COPY --from=osirixfoundation/kheops-beat:latest /install/rpm/metricbeat-x86_64.rpm .
RUN apk add rpm
RUN rpm -vi metricbeat-x86_64.rpm && \
 rm metricbeat-x86_64.rpm && \
 rm /etc/metricbeat/metricbeat.reference.yml && \
 rm /etc/metricbeat/modules.d/*

COPY metricbeat.yml /etc/metricbeat/metricbeat.yml
COPY http.yml /etc/metricbeat/modules.d/http.yml
RUN chmod go-w /etc/metricbeat/metricbeat.yml
RUN chmod go-w /etc/metricbeat/modules.d/http.yml


ENTRYPOINT ["/entrypoint.sh"]
