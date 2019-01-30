docker-compose down -v
( cd ../.. && docker build -t kheops/ui .)
docker-compose up -d
docker logs kheops-ui -f
