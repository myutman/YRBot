FROM gradle:jdk10 as builder
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle clean customFatJar

FROM openjdk:10-jre-slim
RUN apt-get update && apt-get install -y curl
EXPOSE 8000
COPY --from=builder /home/gradle/src/build/libs/server-1.0-SNAPSHOT.jar /java-http-microservice.jar
CMD java -jar /java-http-microservice.jar

