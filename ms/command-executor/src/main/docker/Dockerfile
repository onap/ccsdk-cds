FROM python:3.6-slim

ENV GRPC_PYTHON_VERSION 1.19.0
RUN python -m pip install --upgrade pip
RUN pip install grpcio==${GRPC_PYTHON_VERSION} grpcio-tools==${GRPC_PYTHON_VERSION}
RUN pip install ansible ncclient ciscoconfparse
RUN mkdir -p /opt/onap/blueprints && chmod 777 /opt/onap/blueprints