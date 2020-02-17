FROM alpine:latest AS extractor
COPY @project.build.finalName@-@assembly.id@.tar.gz /source.tar.gz
RUN tar -xzf /source.tar.gz -C /tmp \
 && cp -rf /tmp/@project.build.finalName@/opt / \
 && rm -rf /source.tar.gz \
 && rm -rf /tmp/@project.build.finalName@

FROM omahoco1/alpine-java-python

# add entrypoint
COPY startService.sh /startService.sh
RUN chmod 777 /startService.sh && dos2unix /startService.sh

# add application
COPY --from=extractor /opt /opt

ENTRYPOINT [ "/startService.sh" ]
