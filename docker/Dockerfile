FROM tomcat:8.0.51-jre8

ARG VCS_REF

LABEL org.label-schema.vcs-ref=$VCS_REF \
      org.label-schema.vcs-url="https://github.com/OsiriX-Foundation/KheopsAuthorization"

ENV SECRET_FILE_PATH=/run/secrets/*
ENV REPLACE_FILE_PATH=/usr/local/tomcat/conf/context.xml

COPY KheopsZipper.war /usr/local/tomcat/webapps/zipper.war
