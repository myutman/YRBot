docker kill server postgres
docker pull gcr.io/yrealtor-d5790/postgres-image:tag1
docker pull gcr.io/yrealtor-d5790/server-image:tag4
docker run -d --rm -t -i --name postgres gcr.io/yrealtor-d5790/postgres-image:tag1
docker run -d -p 8000:8000 --rm -t -i --link postgres --name server gcr.io/yrealtor-d5790/server-image:tag4

