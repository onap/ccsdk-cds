FROM onap/integration-python:7.0.1

RUN groupadd -r -g 1000 onap && useradd -u 1000 -r -g onap onap

RUN mkdir -p /opt/app/onap/logs/ && touch /opt/app/onap/logs/application.log

COPY @project.build.finalName@-@assembly.id@.tar.gz /source.tar.gz
RUN tar -xzf /source.tar.gz -C /tmp \
 && cp -rf /tmp/@project.build.finalName@/opt / \
 && rm -rf /source.tar.gz \
 && rm -rf /tmp/@project.build.finalName@

RUN pip install --no-cache-dir -r /opt/app/onap/python/requirements/docker.txt

RUN mkdir -p /opt/app/onap/blueprints/deploy
RUN chown onap:onap /opt -R

VOLUME /opt/app/onap/blueprints/deploy/

USER onap
ENTRYPOINT /opt/app/onap/python/start.sh
