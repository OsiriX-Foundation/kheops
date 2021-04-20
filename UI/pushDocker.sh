read -p "Tag name : " TAG
echo $TAG
docker build -t osirixfoundation/kheops-ui:${TAG} .
docker push osirixfoundation/kheops-ui:${TAG}
