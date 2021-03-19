# Prepare stage for multistage image build
## START OF STAGE0 ##
FROM python:3.7-slim AS stage0

USER root

# add entrypoint
COPY *.sh /opt/app/onap/command-executor/

# add application
COPY @project.build.finalName@-@assembly.id@.tar.gz /source.tar.gz

RUN tar -xzf /source.tar.gz -C /tmp \
    && cp -rf /tmp/@project.build.finalName@/opt / \
    && rm -rf /source.tar.gz \
    && rm -rf /tmp/@project.build.finalName@ \
    && groupadd -r -g 1000 onap && useradd -r -u 1000 -g onap onap \
    && mkdir -p /opt/app/onap/blueprints/deploy /opt/app/onap/logs \
    && touch /opt/app/onap/logs/application.log \
    && chown -R onap:onap /opt \
    && chmod -R 777 /opt

RUN python -m pip install --no-cache-dir --upgrade pip setuptools
RUN pip install --no-cache-dir grpcio==1.20.0 grpcio-tools==1.20.0 virtualenv==16.7.9

## END OF STAGE0 ##


## This will create actual image
FROM python:3.7-slim
USER root

COPY --from=stage0 /opt /opt

USER onap
ENTRYPOINT /opt/app/onap/command-executor/start.sh
