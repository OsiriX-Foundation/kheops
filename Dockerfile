FROM node:9.11.1-alpine

RUN npm install -g http-server
RUN apk update && apk add yarn python g++ make && rm -rf /var/cache/apk/*

WORKDIR /app
COPY package*.json ./

RUN npm install

COPY . .

RUN npm run build

EXPOSE 8080
CMD [ "http-server", "dist" ]
