FROM onap/ccsdk-alpine-j11-image:1.1.0

USER root

# add entrypoint
COPY *.sh /opt/app/onap/blueprints-processor/

# add application
COPY @project.build.finalName@-@assembly.id@.tar.gz /source.tar.gz

RUN tar -xzf /source.tar.gz -C /tmp \
 && cp -rf /tmp/@project.build.finalName@/opt / \
 && rm -rf /source.tar.gz \
 && rm -rf /tmp/@project.build.finalName@ \
 && touch /velocity.log \
 && chown onap:onap /velocity.log \
 && chmod 755 /velocity.log \
 && mkdir -p /opt/app/onap/blueprints/deploy \
 && chown -R onap:onap /opt \
 && chmod -R 777 /opt

USER onap
ENTRYPOINT [ "/opt/app/onap/blueprints-processor/startService.sh" ]
