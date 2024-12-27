FROM openjdk:17-jdk-alpine

COPY target/promscraper-1.0.0-SNAPSHOT-runner.jar promscraper-1.0.0-SNAPSHOT-runner.jar

# COPY target/quarkus-app/quarkus-run.jar target/quarkus-app/quarkus-run.jar

EXPOSE 8080

ENV DOCKER_HOST=tcp://host.docker.internal:2375
ENV TESTCONTAINERS_HOST_OVERRIDE=host.docker.internal

ENTRYPOINT ["java","-jar","/promscraper-1.0.0-SNAPSHOT-runner.jar"]