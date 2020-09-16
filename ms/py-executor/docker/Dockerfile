FROM python:3.7-slim

USER root
RUN mkdir -p /opt/app/onap/logs/ && touch /opt/app/onap/logs/application.log

# add entrypoint
COPY start.sh /opt/app/onap/py-executor/start.sh
# add application
COPY @project.build.finalName@-@assembly.id@.tar.gz /source.tar.gz

RUN tar -xzf /source.tar.gz -C /tmp \
 && cp -rf /tmp/@project.build.finalName@/opt / \
 && rm -rf /source.tar.gz \
 && rm -rf /tmp/@project.build.finalName@ \
 && mkdir -p /opt/app/onap/blueprints/deploy \
 && chmod 755 /opt/app/onap/py-executor/start.sh

VOLUME /opt/app/onap/blueprints/deploy/

RUN python -m pip install --upgrade pip setuptools
RUN pip install --no-cache-dir -r /opt/app/onap/python/requirements/docker.txt

RUN chown onap:onap /opt -R
USER onap
ENTRYPOINT /opt/app/onap/py-executor/start.sh
