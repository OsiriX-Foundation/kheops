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

CMD ["./docker-entrypoint.sh"]
