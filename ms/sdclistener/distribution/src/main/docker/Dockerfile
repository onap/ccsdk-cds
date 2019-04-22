FROM openjdk:8-jdk-alpine

ENV HTTP_PROXY  ${HTTP_PROXY}
ENV HTTPS_PROXY ${HTTPS_PROXY}

# add entrypoint
COPY startService.sh /startService.sh
RUN chmod 751 /startService.sh
# add application
COPY @project.build.finalName@-@assembly.id@.tar.gz /source.tar.gz
RUN tar -xzf /source.tar.gz -C /tmp \
 && cp -rf /tmp/@project.build.finalName@/opt / \
 && rm -rf /source.tar.gz \
 && rm -rf /tmp/@project.build.finalName@

ENTRYPOINT /startService.sh
