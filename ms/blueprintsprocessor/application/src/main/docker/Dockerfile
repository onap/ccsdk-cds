# Prepare stage for multistage image build
## START OF STAGE0 ##
FROM docker.io/eclipse-temurin:21-jre-alpine AS stage0

# add entrypoint
COPY *.sh /opt/app/onap/blueprints-processor/

# add application
COPY @project.build.finalName@-@assembly.id@.tar.gz /source.tar.gz

ARG OTEL_AGENT_VERSION=2.26.1

RUN tar -xzf /source.tar.gz -C /tmp \
 && cp -rf /tmp/@project.build.finalName@/opt / \
 && rm -rf /source.tar.gz \
 && rm -rf /tmp/@project.build.finalName@ \
 && mkdir -p /opt/app/onap/blueprints/deploy \
 && touch /velocity.log \
 && wget -q -O /opt/app/onap/opentelemetry-javaagent.jar \
    "https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v${OTEL_AGENT_VERSION}/opentelemetry-javaagent.jar" \
 && chown -R 1000:1000 /opt /velocity.log \
 && chmod -R 755 /opt /velocity.log

## END OF STAGE0 ##


## This will create actual image
FROM docker.io/eclipse-temurin:21-jre-alpine
RUN addgroup -g 1000 onap && adduser -D -u 1000 -G onap onap

COPY --from=stage0 /opt /opt
COPY --from=stage0 /velocity.log /velocity.log

USER onap
ENTRYPOINT [ "/opt/app/onap/blueprints-processor/startService.sh" ]
