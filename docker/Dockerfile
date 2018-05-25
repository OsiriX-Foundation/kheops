FROM tomcat:8.0.51-jre8

ENV SECRET_FILE_PATH=/run/secrets/*
ENV REPLACE_FILE_PATH=/usr/local/tomcat/conf/*.xml

CMD ["./replaceSecretsAndRun.sh"]

COPY KheopsAuthorization.war /usr/local/tomcat/webapps/KheopsAuthorization.war
COPY context.xml /usr/local/tomcat/conf/context.xml
COPY replaceSecretsAndRun.sh replaceSecretsAndRun.sh

RUN chmod +x replaceSecretsAndRun.sh
