FROM onap/ccsdk-alpine-j11-image:1.0.1

USER root
# add entrypoint
COPY startService.sh /opt/app/onap/sdc-listener/startService.sh
# add application
COPY @project.build.finalName@-@assembly.id@.tar.gz /source.tar.gz

RUN tar -xzf /source.tar.gz -C /tmp \
 && cp -rf /tmp/@project.build.finalName@/opt / \
 && rm -rf /source.tar.gz \
 && rm -rf /tmp/@project.build.finalName@ \
 && mkdir -p /opt/app/onap/cds-sdc-listener \
 && chown onap:onap /opt -R \
 && chmod 755 /opt/app/onap/sdc-listener/startService.sh

USER onap
ENTRYPOINT /opt/app/onap/sdc-listener/startService.sh
