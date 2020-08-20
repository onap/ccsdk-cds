FROM onap/ccsdk-alpine-j11-image

RUN addgroup -S -g 1000 onap && adduser -S onap -u 1000 -G ona
# add entrypoint
COPY startService.sh /startService.sh
RUN chown onap:onap /startService.sh && chmod 751 /startService.sh
# add application
COPY @project.build.finalName@-@assembly.id@.tar.gz /source.tar.gz
RUN tar -xzf /source.tar.gz -C /tmp \
 && cp -rf /tmp/@project.build.finalName@/opt / \
 && rm -rf /source.tar.gz \
 && rm -rf /tmp/@project.build.finalName@ \
 && mkdir -p /opt/app/onap/cds-sdc-listener \
 && chown onap:onap /opt -R

USER onap
ENTRYPOINT /startService.sh
