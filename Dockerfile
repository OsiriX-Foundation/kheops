FROM node:10-alpine as build-stage
RUN apk update && apk add yarn python g++ make && rm -rf /var/cache/apk/*
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
COPY .env.prod .env
RUN npm run build

FROM nginx:stable as production-stage

ENV SECRET_FILE_PATH=/run/secrets

COPY --from=build-stage /app/script/ui.conf /etc/nginx/conf.d/ui.conf
COPY --from=build-stage /app/dist /usr/share/nginx/html
COPY --from=build-stage /app/src/assets /usr/share/nginx/html/assets
COPY --from=build-stage /app/script/docker-entrypoint-nginx.sh /docker-entrypoint.sh

ENV SERVER_NAME=localhost

RUN chmod +x /docker-entrypoint.sh

EXPOSE 3000

#elastic
COPY --from=build-stage /app/docker/metricbeat.conf /etc/nginx/conf.d/metricbeat.conf
COPY --from=osirixfoundation/kheops-beat:latest /install/deb/ .

#METRICBEAT
RUN dpkg -i metricbeat-amd64.deb
RUN rm metricbeat-amd64.deb

COPY --from=build-stage /app/docker/metricbeat.yml /etc/metricbeat/metricbeat.yml
RUN chmod go-w /etc/metricbeat/metricbeat.yml

#RUN metricbeat modules enable nginx
COPY --from=build-stage /app/docker/metricbeat_nginx.yml /etc/metricbeat/modules.d/nginx.yml
RUN chmod go-w /etc/metricbeat/modules.d/nginx.yml


#FILEBEAT
RUN dpkg -i filebeat-amd64.deb
RUN rm filebeat-amd64.deb

COPY --from=build-stage /app/docker/filebeat.yml /etc/filebeat/filebeat.yml
RUN chmod go-w /etc/filebeat/filebeat.yml

#RUN filebeat modules enable nginx
COPY --from=build-stage /app/docker/filebeat_nginx.yml /etc/filebeat/modules.d/nginx.yml
RUN chmod go-w /etc/filebeat/modules.d/nginx.yml

RUN rm /var/log/nginx/access.log /var/log/nginx/error.log

CMD ["./docker-entrypoint.sh"]
