FROM tomcat:9.0.13-jre8

ARG VCS_REF

LABEL org.label-schema.vcs-ref=$VCS_REF \
      org.label-schema.vcs-url="https://github.com/OsiriX-Foundation/KheopsAuthorization"

ENV REPLACE_FILE_PATH=/usr/local/tomcat/conf/context.xml

COPY context.xml /usr/local/tomcat/conf/context.xml

COPY replaceSecretsAndRun.sh replaceSecretsAndRun.sh
RUN chmod +x replaceSecretsAndRun.sh

COPY KheopsZipper.war /usr/local/tomcat/webapps/zipper.war

CMD ["./replaceSecretsAndRun.sh"]
