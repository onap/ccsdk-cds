FROM alpine:latest AS extractor
COPY @project.build.finalName@-@assembly.id@.tar.gz /source.tar.gz
RUN tar -xzf /source.tar.gz -C /tmp \
 && cp -rf /tmp/@project.build.finalName@/opt / \
 && rm -rf /source.tar.gz \
 && rm -rf /tmp/@project.build.finalName@

FROM openjdk:11.0.8-jre-slim-buster

# add entrypoint
COPY startService.sh /startService.sh
RUN adduser --uid 1000 --system --group onap \
 && chown onap:onap /startService.sh \
 && touch /velocity.log && chmod 755 /velocity.log && chown onap:onap /velocity.log \
 && chmod 755 /startService.sh
# dos2unix /startService.sh is redundant. Pls fix your git settings!

# add application
COPY --from=extractor /opt /opt
RUN mkdir -p /opt/app/onap/blueprints/deploy && chown onap:onap /opt -R
USER onap

ENTRYPOINT [ "/startService.sh" ]
