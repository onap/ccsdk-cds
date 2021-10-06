FROM python:3.7-slim
USER root

# add entrypoint
COPY *.sh /opt/app/onap/command-executor/

# add application
COPY @project.build.finalName@-@assembly.id@.tar.gz /source.tar.gz

RUN tar -xzf /source.tar.gz -C /tmp \
    && cp -rf /tmp/@project.build.finalName@/opt / \
    && rm -rf /source.tar.gz \
    && rm -rf /tmp/@project.build.finalName@ \
    && addgroup onap -g 1000 && adduser -h /home/onap -G onap -S -D -u 1000 onap \
    && mkdir -p /opt/app/onap/blueprints/deploy /opt/app/onap/logs \
    && touch /opt/app/onap/logs/application.log \
    && chown -R onap:onap /opt \
    && chmod -R 755 /opt

RUN python -m pip install --no-cache-dir --upgrade pip setuptools
RUN pip install --no-cache-dir requests==2.26.0 grpcio==1.20.0 grpcio-tools==1.20.0 virtualenv==16.7.9 prometheus-client==0.11.0

USER onap
ENTRYPOINT /opt/app/onap/command-executor/start.sh
