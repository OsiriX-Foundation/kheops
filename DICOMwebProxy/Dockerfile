FROM gradle:6.9.2-jdk11 AS build
WORKDIR /home/gradle/capabilities
COPY --chown=gradle:gradle build.gradle /home/gradle/capabilities/build.gradle
COPY --chown=gradle:gradle src /home/gradle/capabilities/src
RUN gradle war --no-daemon --info

FROM tomcat:9.0.58-jdk11

COPY --from=build /home/gradle/capabilities/build/libs/capabilities.war /usr/local/tomcat/webapps/capabilities.war
COPY setenv.sh $CATALINA_HOME/bin/setenv.sh

ENV JAVA_TOOL_OPTIONS -Xdebug -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005