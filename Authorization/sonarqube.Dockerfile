FROM gradle:6.8.3-jdk11 AS build
WORKDIR /home/gradle/authorization
COPY --chown=gradle:gradle build.gradle /home/gradle/authorization/build.gradle
COPY --chown=gradle:gradle src /home/gradle/authorization/src
