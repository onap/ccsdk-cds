FROM python:3.6-slim

ENV GRPC_PYTHON_VERSION 1.19.0
RUN python -m pip install --upgrade pip
RUN pip install grpcio==${GRPC_PYTHON_VERSION} grpcio-tools==${GRPC_PYTHON_VERSION}
RUN pip install virtualenv

RUN mkdir -p /opt/onap/blueprints && mkdir -p /opt/onap/app/python && chmod -R 777 /opt/onap

COPY start.sh /opt/onap/app/start.sh
RUN chmod u+x /opt/onap/app/start.sh

COPY @project.build.finalName@-@assembly.id@.tar.gz /source.tar.gz
RUN tar -xzf /source.tar.gz -C /tmp \
 && cp -rf /tmp/@project.build.finalName@/opt / \
 && rm -rf /source.tar.gz \
 && rm -rf /tmp/@project.build.finalName@

ENTRYPOINT /opt/onap/app/start.sh