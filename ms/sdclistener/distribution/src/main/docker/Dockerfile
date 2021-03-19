# Prepare stage for multistage image build
## START OF STAGE0 ##
FROM onap/ccsdk-alpine-j11-image:1.1.2 AS stage0

USER root

# add entrypoint
COPY *.sh /opt/app/onap/sdc-listener/

# add application
COPY @project.build.finalName@-@assembly.id@.tar.gz /source.tar.gz

RUN tar -xzf /source.tar.gz -C /tmp \
 && cp -rf /tmp/@project.build.finalName@/opt / \
 && rm -rf /source.tar.gz \
 && rm -rf /tmp/@project.build.finalName@ \
 && mkdir -p /opt/app/onap/cds-sdc-listener \
 && chown -R onap:onap /opt \
 && chmod -R 777 /opt

## END OF STAGE0 ##


## This will create actual image
FROM onap/ccsdk-alpine-j11-image:1.1.2
USER root

COPY --from=stage0 /opt /opt

USER onap
ENTRYPOINT /opt/app/onap/sdc-listener/startService.sh
