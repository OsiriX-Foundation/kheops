FROM docker.elastic.co/beats/filebeat:7.1.1

COPY filebeat.yml /filebeattmp/filebeat.yml

COPY entrypoint.sh /entrypoint.sh

USER root

RUN chmod 770 /usr/share/filebeat
RUN chmod -R 777 /filebeattmp 
RUN chmod +x /entrypoint.sh

USER filebeat


ENTRYPOINT ["/entrypoint.sh"]
