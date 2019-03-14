FROM node:9.11.1-alpine as build-stage
RUN apk update && apk add yarn python g++ make && rm -rf /var/cache/apk/*
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
COPY .env.prod .env
RUN npm run build

FROM nginx:stable-alpine as production-stage
COPY --from=build-stage /app/script/ui.conf /etc/nginx/conf.d/ui.conf
COPY --from=build-stage /app/dist /usr/share/nginx/html
COPY --from=build-stage /app/src/assets /usr/share/nginx/html/assets
COPY --from=build-stage /app/script/docker-entrypoint-nginx.sh /docker-entrypoint.sh

ENV SERVER_NAME=localhost

RUN chmod +x /docker-entrypoint.sh

EXPOSE 3000

#elastic
COPY metricbeat.conf /etc/nginx/conf.d/metricbeat.conf

RUN apt-get update
RUN apt-get install -y curl

#METRICBEAT
ENV METRICBEAT_VERSION 6.6.1
RUN curl -L -O https://artifacts.elastic.co/downloads/beats/metricbeat/metricbeat-${METRICBEAT_VERSION}-amd64.deb
RUN dpkg -i metricbeat-${METRICBEAT_VERSION}-amd64.deb
RUN rm metricbeat-${METRICBEAT_VERSION}-amd64.deb

COPY metricbeat.yml /etc/metricbeat/metricbeat.yml
RUN chmod go-w /etc/metricbeat/metricbeat.yml

#RUN metricbeat modules enable nginx
COPY metricbeat_nginx.yml /etc/metricbeat/modules.d/nginx.yml
RUN chmod go-w /etc/metricbeat/modules.d/nginx.yml


#FILEBEAT
ENV FILEBEAT_VERSION 6.6.1
RUN curl -L -O https://artifacts.elastic.co/downloads/beats/filebeat/filebeat-${FILEBEAT_VERSION}-amd64.deb
RUN dpkg -i filebeat-${FILEBEAT_VERSION}-amd64.deb
RUN rm filebeat-${FILEBEAT_VERSION}-amd64.deb

COPY filebeat.yml /etc/filebeat/filebeat.yml
RUN chmod go-w /etc/filebeat/filebeat.yml

#RUN filebeat modules enable nginx
COPY filebeat_nginx.yml /etc/filebeat/modules.d/nginx.yml
RUN chmod go-w /etc/filebeat/modules.d/nginx.yml


RUN apt-get remove -y curl

RUN rm /var/log/nginx/access.log /var/log/nginx/error.log

CMD ["./docker-entrypoint.sh"]
