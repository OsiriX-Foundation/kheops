# Using JDK 17 now
FROM gradle:7.6.4-jdk17 AS build
WORKDIR /home/gradle/authorization
COPY --chown=gradle:gradle build.gradle /home/gradle/authorization/build.gradle
COPY --chown=gradle:gradle src /home/gradle/authorization/src
