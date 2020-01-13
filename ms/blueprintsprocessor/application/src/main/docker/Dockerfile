FROM alpine:latest AS extractor
COPY @project.build.finalName@-@assembly.id@.tar.gz /source.tar.gz
RUN tar -xzf /source.tar.gz -C /tmp \
 && cp -rf /tmp/@project.build.finalName@/opt / \
 && rm -rf /source.tar.gz \
 && rm -rf /tmp/@project.build.finalName@

FROM openjdk:11.0.5-jre-slim-buster

# add entrypoint
COPY startService.sh /startService.sh
RUN chmod 755 /startService.sh

# add application
COPY --from=extractor /opt /opt

ENTRYPOINT [ "/startService.sh" ]
