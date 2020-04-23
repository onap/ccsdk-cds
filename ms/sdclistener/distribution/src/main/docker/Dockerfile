FROM openjdk:8-jdk-alpine

RUN addgroup -S onap && adduser -S onap -G onap
# add entrypoint
COPY startService.sh /startService.sh
RUN chown onap:onap /startService.sh
RUN chmod 751 /startService.sh
# add application
COPY @project.build.finalName@-@assembly.id@.tar.gz /source.tar.gz
RUN tar -xzf /source.tar.gz -C /tmp \
 && cp -rf /tmp/@project.build.finalName@/opt / \
 && rm -rf /source.tar.gz \
 && rm -rf /tmp/@project.build.finalName@

RUN mkdir -p /opt/app/onap/cds-sdc-listener
RUN chown onap:onap /opt -R

USER onap
ENTRYPOINT /startService.sh
