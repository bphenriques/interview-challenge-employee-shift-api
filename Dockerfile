FROM adoptopenjdk/openjdk11:debian-slim as BUILD_IMAGE
MAINTAINER Bruno Henriques

ARG DOCKERIZE_VERSION=v0.6.1
ENV GRADLE_OPTS "-Dorg.gradle.daemon=false"
ENV APP_HOME /app

WORKDIR $APP_HOME

RUN apt-get update && apt-get install -y wget && \
        wget https://github.com/jwilder/dockerize/releases/download/$DOCKERIZE_VERSION/dockerize-linux-amd64-$DOCKERIZE_VERSION.tar.gz \
        && tar -C /usr/local/bin -xzvf dockerize-linux-amd64-$DOCKERIZE_VERSION.tar.gz \
        && rm dockerize-linux-amd64-$DOCKERIZE_VERSION.tar.gz

# Add Source Files - Main build files
COPY build.gradle.kts $APP_HOME
COPY settings.gradle.kts $APP_HOME
COPY gradlew $APP_HOME
COPY gradle $APP_HOME/gradle
COPY buildSrc $APP_HOME/buildSrc

# Add Source Files - Main src
COPY common-test $APP_HOME/common-test
COPY domain $APP_HOME/domain
COPY infrastructure $APP_HOME/infrastructure
COPY web-app $APP_HOME/web-app
COPY db $APP_HOME/db

# Build the Project
RUN $APP_HOME/gradlew clean build -x test -x integrationTest -x acceptanceTest

# Exposed ports.
EXPOSE 8080
EXPOSE 8081

# Build PRD image with the binaries only.
# The image can further slimified if we use the JRE (runtime) instead which is better.
FROM adoptopenjdk/openjdk11:debian-slim as PRD_IMAGE
WORKDIR /app

COPY --from=BUILD_IMAGE /app/web-app/build/libs/employee-shift-api.jar .
COPY --from=BUILD_IMAGE /app/db/migration ./db/migration

EXPOSE 8080
EXPOSE 8081
