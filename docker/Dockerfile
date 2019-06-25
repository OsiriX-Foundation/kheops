FROM tomcat:9-jdk8

ARG VCS_REF

LABEL org.label-schema.vcs-ref=$VCS_REF \
      org.label-schema.vcs-url="https://github.com/OsiriX-Foundation/KheopsZipper"

ENV SECRET_FILE_PATH=/run/secrets
ENV REPLACE_FILE_PATH=/usr/local/tomcat/conf/context.xml

COPY KheopsZipper.war /usr/local/tomcat/webapps/zipper.war
COPY context.xml /usr/local/tomcat/conf/context.xml

COPY replaceSecretsAndRun.sh replaceSecretsAndRun.sh

CMD ["./replaceSecretsAndRun.sh"]
