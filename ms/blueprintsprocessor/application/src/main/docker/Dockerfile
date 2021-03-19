# Prepare stage for multistage image build
## START OF STAGE0 ##
FROM onap/ccsdk-alpine-j11-image:1.1.0 AS stage0
USER root

# add entrypoint
COPY *.sh /opt/app/onap/blueprints-processor/

# add application
COPY @project.build.finalName@-@assembly.id@.tar.gz /source.tar.gz

RUN tar -xzf /source.tar.gz -C /tmp \
 && cp -rf /tmp/@project.build.finalName@/opt / \
 && rm -rf /source.tar.gz \
 && rm -rf /tmp/@project.build.finalName@ \
 && mkdir -p /opt/app/onap/blueprints/deploy \
 && touch /velocity.log \
 && chown -R onap:onap /opt /velocity.log
 && chmod -R 777 /opt /velocity.log

## END OF STAGE0 ##


## This will create actual image
FROM onap/ccsdk-alpine-j11-image:1.1.0
USER root

COPY --from=stage0 /opt /opt
COPY --from=stage0 /velocity.log /velocity.log

USER onap
ENTRYPOINT [ "/opt/app/onap/blueprints-processor/startService.sh" ]
