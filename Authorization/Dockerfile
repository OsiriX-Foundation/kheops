FROM gradle:7.6.4-jdk17 AS build
ENV APP_HOME=/home/gradle/authorization
WORKDIR $APP_HOME
COPY --chown=gradle:gradle build.gradle gradlew $APP_HOME
COPY gradle $APP_HOME/gradle
RUN ./gradlew build || return 0 
COPY --chown=gradle:gradle src ${APP_HOME}/src
RUN gradle war --no-daemon --info

FROM tomcat:9-jdk17-corretto
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
