FROM python:3.7-slim

ENV HTTP_PROXY  ${HTTP_PROXY}
ENV HTTPS_PROXY ${HTTPS_PROXY}

RUN mkdir -p /opt/app/onap/logs/ && touch /opt/app/onap/logs/application.log

COPY @project.build.finalName@-@assembly.id@.tar.gz /source.tar.gz
RUN tar -xzf /source.tar.gz -C /tmp \
 && cp -rf /tmp/@project.build.finalName@/opt / \
 && rm -rf /source.tar.gz \
 && rm -rf /tmp/@project.build.finalName@

RUN pip install --no-cache-dir -r /opt/app/onap/requirements.txt

VOLUME /opt/app/onap/blueprints/deploy/

ENTRYPOINT /opt/app/onap/start.sh