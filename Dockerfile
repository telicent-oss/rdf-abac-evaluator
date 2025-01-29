# syntax=docker/dockerfile:1.7

FROM telicent/telicent-java21:1.1.2 AS rdf-abac-evaluator

USER root

# Define argument with a default value to prevent build failures
ARG PROJECT_VERSION

# Set up directories and user in a single efficient step
RUN groupadd -r telicent-service && \
    useradd -r -g telicent-service -d /app telicent-service && \
    mkdir -p /app /config /opt/telicent/sbom && \
    chown -R telicent-service:telicent-service /app /config /opt/telicent/sbom

WORKDIR /app
USER telicent-service

# Copy config and application files
COPY --chown=telicent-service:telicent-service src/main/resources/ /config/
COPY --chown=telicent-service:telicent-service target/rdf-abac-evaluator-${PROJECT_VERSION}.jar /app/app.jar
COPY --chown=telicent-service:telicent-service target/rdf-abac-evaluator-${PROJECT_VERSION}-bom.json /opt/telicent/sbom/

ENTRYPOINT /usr/bin/dumb-init -v --single-child -- java -cp /app/app.jar $CLASSNAME $ARGS