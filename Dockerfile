FROM node:9.11.1-alpine as build-stage
RUN apk update && apk add yarn python g++ make && rm -rf /var/cache/apk/*
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

FROM nginx:stable-alpine as production-stage
COPY --from=build-stage /app/dist /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
