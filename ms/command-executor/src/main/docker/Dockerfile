FROM python:3.6-slim

ENV GRPC_PYTHON_VERSION 1.19.0
RUN python -m pip install --upgrade pip
RUN pip install grpcio==${GRPC_PYTHON_VERSION} grpcio-tools==${GRPC_PYTHON_VERSION}
RUN pip install ansible ncclient ciscoconfparse

RUN mkdir -p /opt/onap/blueprints && mkdir -p /opt/onap/app/python && chmod -R 777 /opt/onap

COPY @project.build.finalName@-@assembly.id@.tar.gz /source.tar.gz
RUN tar -xzf /source.tar.gz -C /tmp \
 && cp -rf /tmp/@project.build.finalName@/opt / \
 && rm -rf /source.tar.gz \
 && rm -rf /tmp/@project.build.finalName@

ENTRYPOINT cd /opt/onap/app/python/ && python command_executor_server.py