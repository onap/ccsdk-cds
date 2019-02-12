FROM anapsix/alpine-java:8_jdk

ENV HTTP_PROXY  ${HTTP_PROXY}
ENV HTTPS_PROXY ${HTTPS_PROXY}

RUN apk add --no-cache curl

COPY startService.sh /startService.sh
RUN chmod 777 /startService.sh && dos2unix /startService.sh

COPY @project.build.finalName@-@assembly.id@.tar.gz /source.tar.gz

RUN (mkdir -p /source /opt/app/onap) && (tar -xzf /source.tar.gz -C /source) \
&& (mv /source/@project.build.finalName@ /source/app) \
&& (cp -rf /source/app/opt/app/onap/lib /opt/app/onap/) \
&& (cp -rf /source/app/etc /) \
&& (cp -rf /source/app/config /) \
&& (cp -rf /source/app/scripts /opt/app/onap/) \
&& (rm -rf /source)

ENTRYPOINT /startService.sh