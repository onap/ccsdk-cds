FROM alpine:latest AS extractor
COPY @project.build.finalName@-@assembly.id@.tar.gz /source.tar.gz
RUN tar -xzf /source.tar.gz -C /tmp \
 && cp -rf /tmp/@project.build.finalName@/opt / \
 && rm -rf /source.tar.gz \
 && rm -rf /tmp/@project.build.finalName@

FROM omahoco1/alpine-java-python

# add entrypoint
COPY startService.sh /startService.sh
RUN addgroup -S -g 1000 onap && adduser -u 1000 -S onap -G onap
RUN chown onap:onap /startService.sh
RUN touch /velocity.log && chmod 777 /velocity.log
RUN chown onap:onap /velocity.log
RUN chmod 777 /startService.sh && dos2unix /startService.sh

# add application
COPY --from=extractor /opt /opt
RUN mkdir -p /opt/app/onap/blueprints/deploy
RUN chown onap:onap /opt -R
USER onap

ENTRYPOINT [ "/startService.sh" ]
