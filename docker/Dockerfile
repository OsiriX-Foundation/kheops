FROM tomcat:9-jdk8

ARG VCS_REF

LABEL org.label-schema.vcs-ref=$VCS_REF \
      org.label-schema.vcs-url="https://github.com/OsiriX-Foundation/KheopsProxy"

ENV SECRET_FILE_PATH=/run/secrets
ENV REPLACE_FILE_PATH=/usr/local/tomcat/conf/context.xml

COPY KheopsDICOMwebProxy.war /usr/local/tomcat/webapps/capabilities.war
COPY context.xml /usr/local/tomcat/conf/context.xml

#FILEBEAT
COPY --from=osirixfoundation/kheops-beat:latest /install/deb/filebeat-amd64.deb .
RUN dpkg -i filebeat-amd64.deb && \
 rm filebeat-amd64.deb && \
 rm /etc/filebeat/filebeat.reference.yml && \
 rm /etc/filebeat/modules.d/*

COPY filebeat/filebeat.yml /etc/filebeat/filebeat.yml
RUN chmod go-w /etc/filebeat/filebeat.yml

COPY replaceSecretsAndRun.sh replaceSecretsAndRun.sh

CMD ["./replaceSecretsAndRun.sh"]
