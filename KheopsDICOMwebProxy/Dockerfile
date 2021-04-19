FROM gradle:6.5-jdk11 AS build
WORKDIR /home/gradle/capabilities
COPY --chown=gradle:gradle build.gradle /home/gradle/capabilities/build.gradle
COPY --chown=gradle:gradle src /home/gradle/capabilities/src
RUN gradle war --no-daemon --info

FROM tomcat:9.0.35-jdk11
ARG VCS_REF
LABEL org.label-schema.vcs-ref=$VCS_REF \
      org.label-schema.vcs-url="https://github.com/OsiriX-Foundation/KheopsDICOMwebProxy"

COPY --from=build /home/gradle/capabilities/build/libs/capabilities.war /usr/local/tomcat/webapps/capabilities.war
COPY setenv.sh $CATALINA_HOME/bin/setenv.sh
COPY kheops-entrypoint.sh /kheops-entrypoint.sh

#FILEBEAT
COPY --from=osirixfoundation/kheops-beat:latest /install/deb/filebeat-amd64.deb .
RUN dpkg -i filebeat-amd64.deb && \
 rm filebeat-amd64.deb && \
 rm /etc/filebeat/filebeat.reference.yml && \
 rm /etc/filebeat/modules.d/*

COPY filebeat/filebeat.yml /etc/filebeat/filebeat.yml
RUN chmod go-w /etc/filebeat/filebeat.yml

CMD ["catalina.sh", "run"]
ENTRYPOINT ["/kheops-entrypoint.sh"]
