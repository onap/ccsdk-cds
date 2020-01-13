FROM openjdk:11.0.5-jre-slim-buster

# add entrypoint
COPY startService.sh /startService.sh
RUN chmod 755 /startService.sh && dos2unix /startService.sh

# add application
COPY @project.build.finalName@-@assembly.id@.tar.gz /source.tar.gz
RUN tar -xzf /source.tar.gz -C /tmp \
 && cp -rf /tmp/@project.build.finalName@/opt / \
 && rm -rf /source.tar.gz \
 && rm -rf /tmp/@project.build.finalName@

ENTRYPOINT [ "/startService.sh" ]
