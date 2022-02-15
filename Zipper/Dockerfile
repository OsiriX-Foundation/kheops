FROM gradle:7.4.0-jdk11 AS build
WORKDIR /home/gradle/zipper
COPY --chown=gradle:gradle build.gradle /home/gradle/zipper/build.gradle
COPY --chown=gradle:gradle src /home/gradle/zipper/src
RUN gradle war --no-daemon --info

FROM tomcat:9.0.58-jdk11


ENV SECRET_FILE_PATH=/run/secrets
ENV REPLACE_FILE_PATH=/usr/local/tomcat/conf/context.xml

COPY --from=build /home/gradle/zipper/build/libs/zipper.war /usr/local/tomcat/webapps/zipper.war
COPY context.xml /usr/local/tomcat/conf/context.xml
COPY replaceSecretsAndRun.sh replaceSecretsAndRun.sh
RUN chmod +x replaceSecretsAndRun.sh

CMD ["./replaceSecretsAndRun.sh"]
