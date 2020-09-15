FROM onap/integration-python:7.0.1

USER root
ENV GRPC_PYTHON_VERSION 1.20.0

RUN python -m pip install --upgrade pip
RUN pip install grpcio==${GRPC_PYTHON_VERSION} grpcio-tools==${GRPC_PYTHON_VERSION}
RUN pip install virtualenv==16.7.9
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
 && chown onap:onap /opt -R \
 && chmod 755 /opt/app/onap/command-executor/start.sh

VOLUME /opt/app/onap/blueprints/deploy/

USER onap
ENTRYPOINT /opt/app/onap/command-executor/start.sh
