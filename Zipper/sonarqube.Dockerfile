FROM gradle:6.7.1-jdk11 AS build
WORKDIR /home/gradle/zipper
COPY --chown=gradle:gradle build.gradle /home/gradle/zipper/build.gradle
COPY --chown=gradle:gradle src /home/gradle/zipper/src