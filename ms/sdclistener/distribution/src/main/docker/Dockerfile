# Prepare stage for multistage image build
## START OF STAGE0 ##
FROM onap/ccsdk-alpine-j17-image:1.5.1 AS stage0

USER root

# add entrypoint
COPY *.sh /opt/app/onap/sdc-listener/

# add application
COPY @project.build.finalName@-@assembly.id@.tar.gz /source.tar.gz

ARG OTEL_AGENT_VERSION=2.25.0

RUN tar -xzf /source.tar.gz -C /tmp \
 && cp -rf /tmp/@project.build.finalName@/opt / \
 && rm -rf /source.tar.gz \
 && rm -rf /tmp/@project.build.finalName@ \
 && mkdir -p /opt/app/onap/cds-sdc-listener \
 && wget -q -O /opt/app/onap/opentelemetry-javaagent.jar \
    "https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v${OTEL_AGENT_VERSION}/opentelemetry-javaagent.jar" \
 && chown -R onap:onap /opt \
 && chmod -R 755 /opt

## END OF STAGE0 ##


## This will create actual image
FROM onap/ccsdk-alpine-j17-image:1.5.1
USER root

COPY --from=stage0 /opt /opt

USER onap
ENTRYPOINT /opt/app/onap/sdc-listener/startService.sh
