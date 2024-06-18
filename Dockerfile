#
# Copyright (C) Telicent Ltd
#

# syntax=docker/dockerfile:1.7

FROM eclipse-temurin:21-jre

WORKDIR /app

RUN mkdir /config

COPY src/main/resources/* /config/

COPY target/rdf-abac-evaluator-1.1-SNAPSHOT.jar /app/app.jar
ENTRYPOINT java -cp /app/app.jar $CLASSNAME $ARGS