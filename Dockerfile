#
# Copyright (C) Telicent Ltd
#

# syntax=docker/dockerfile:1.7

FROM eclipse-temurin:21-jre as rdf-abac-evaluator

WORKDIR /app

RUN mkdir /config

COPY src/main/resources/* /config/

ARG PROJECT_VERSION

COPY target/rdf-abac-evaluator-${PROJECT_VERSION}.jar /app/app.jar
ENTRYPOINT java -cp /app/app.jar $CLASSNAME $ARGS