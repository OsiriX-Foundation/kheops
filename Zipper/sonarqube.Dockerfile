FROM gradle:7.6.4-jdk17 AS build
WORKDIR /home/gradle/zipper
COPY --chown=gradle:gradle build.gradle /home/gradle/zipper/build.gradle
COPY --chown=gradle:gradle src /home/gradle/zipper/src