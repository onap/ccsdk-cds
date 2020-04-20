FROM python:3.6-slim

ENV GRPC_PYTHON_VERSION 1.20.0
RUN python -m pip install --upgrade pip
RUN pip install grpcio==${GRPC_PYTHON_VERSION} grpcio-tools==${GRPC_PYTHON_VERSION}
RUN pip install virtualenv==16.7.9 pympler==0.8

RUN groupadd -r onap && useradd -r -g onap onap

COPY start.sh /opt/app/onap/start.sh
RUN chmod u+x /opt/app/onap/start.sh

RUN mkdir -p /opt/app/onap/logs/ && touch /opt/app/onap/logs/application.log
RUN chown onap:onap /opt -R

COPY @project.build.finalName@-@assembly.id@.tar.gz /source.tar.gz
RUN tar -xzf /source.tar.gz -C /tmp \
 && cp -rf /tmp/@project.build.finalName@/opt / \
 && rm -rf /source.tar.gz \
 && rm -rf /tmp/@project.build.finalName@

VOLUME /opt/app/onap/blueprints/deploy/
USER onap
ENTRYPOINT /opt/app/onap/start.sh
