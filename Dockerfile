# syntax=docker/dockerfile:1.7

FROM telicent/telicent-java21:1.2.40 AS rdf-abac-evaluator

USER root

# Define argument with a default value to prevent build failures
ARG PROJECT_VERSION

# Set up directories and user in a single efficient step
RUN mkdir -p /app /config /opt/telicent/sbom && \
    chown -R user:user /app /config /opt/telicent/sbom

WORKDIR /app

# Copy config and application files
COPY --chown=user:user src/main/resources/ /config/
COPY --chown=user:user target/rdf-abac-evaluator-${PROJECT_VERSION}.jar /app/app.jar
COPY --chown=user:user target/rdf-abac-evaluator-${PROJECT_VERSION}-bom.json /opt/telicent/sbom/

USER user

ENTRYPOINT /usr/bin/dumb-init -v --single-child -- java -cp /app/app.jar $CLASSNAME $ARGS