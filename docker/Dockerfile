FROM tomcat:8.0.51-jre8

COPY KheopsAuthorization.war /usr/local/tomcat/webapps/KheopsAuthorization.war
COPY context.xml /usr/local/tomcat/conf/context.xml
COPY setenv.sh /usr/local/tomcat/bin/setenv.sh

RUN chmod +x /usr/local/tomcat/bin/setenv.sh
