FROM gradle:7.6.4-jdk17 AS build
WORKDIR /home/gradle/capabilities
COPY --chown=gradle:gradle build.gradle /home/gradle/capabilities/build.gradle
COPY --chown=gradle:gradle src /home/gradle/capabilities/src
