docker-compose down -v
docker rmi kheops-ui
( cd .. && docker build -t kheops-ui .)
docker-compose up -d
docker logs kheops-ui -f
