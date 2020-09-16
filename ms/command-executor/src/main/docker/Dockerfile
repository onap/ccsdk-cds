FROM python:3.7-slim

USER root
RUN mkdir -p /opt/app/onap/logs/ && touch /opt/app/onap/logs/application.log

# add entrypoint
COPY start.sh /opt/app/onap/command-executor/start.sh
# add application
COPY @project.build.finalName@-@assembly.id@.tar.gz /source.tar.gz

RUN tar -xzf /source.tar.gz -C /tmp \
 && cp -rf /tmp/@project.build.finalName@/opt / \
 && rm -rf /source.tar.gz \
 && rm -rf /tmp/@project.build.finalName@ \
 && mkdir -p /opt/app/onap/blueprints/deploy \
 && chmod 755 /opt/app/onap/command-executor/start.sh

VOLUME /opt/app/onap/blueprints/deploy/

RUN python -m pip install --upgrade pip setuptools
RUN pip install grpcio==1.20.0 grpcio-tools==1.20.0
RUN pip install virtualenv==16.7.9

RUN groupadd -r -g 1000 onap && useradd -r -u 1000 -g onap onap
RUN chown onap:onap /opt -R
USER onap
ENTRYPOINT /opt/app/onap/command-executor/start.sh
