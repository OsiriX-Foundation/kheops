FROM gradle:7.6.4-jdk17 AS build
ENV APP_HOME=/home/gradle/capabilities
WORKDIR $APP_HOME
COPY --chown=gradle:gradle build.gradle $APP_HOME
# COPY gradle $APP_HOME/gradle
RUN gradle build --no-daemon || return 0
COPY --chown=gradle:gradle src ${APP_HOME}/src
RUN gradle war --no-daemon --info

FROM tomcat:9-jdk17-corretto

COPY --from=build /home/gradle/capabilities/build/libs/capabilities.war /usr/local/tomcat/webapps/capabilities.war
COPY setenv.sh $CATALINA_HOME/bin/setenv.sh

ENV JAVA_TOOL_OPTIONS -Xdebug -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005