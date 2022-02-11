FROM gradle:6.9.2-jdk11 AS build
WORKDIR /home/gradle/authorization
COPY --chown=gradle:gradle build.gradle /home/gradle/authorization/build.gradle
COPY --chown=gradle:gradle src /home/gradle/authorization/src
RUN gradle war --no-daemon --info

FROM tomcat:9.0.58-jdk11
ARG VCS_REF
LABEL org.label-schema.vcs-ref=$VCS_REF \
      org.label-schema.vcs-url="https://github.com/OsiriX-Foundation/KheopsAuthorization"

ENV SECRET_FILE_PATH=/run/secrets \
    REPLACE_FILE_PATH=/usr/local/tomcat/conf/context.xml

COPY --from=build /home/gradle/authorization/build/libs/authorization.war /usr/local/tomcat/webapps/authorization.war
COPY setenv.sh $CATALINA_HOME/bin/setenv.sh
COPY logging.properties /usr/local/tomcat/conf/logging.properties
COPY context.xml /usr/local/tomcat/conf/context.xml
RUN sed -i '/\/Host/i\\t<Valve className="org.apache.catalina.valves.ErrorReportValve" showReport="false" showServerInfo="false" />' /usr/local/tomcat/conf/server.xml