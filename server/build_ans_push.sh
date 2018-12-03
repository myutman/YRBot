docker build . -t server-image --no-cache
docker tag server-image gcr.io/yrealtor-d5790/server-image:tag4
docker push gcr.io/yrealtor-d5790/server-image:tag4
