FROM python:3.9-slim
USER root

# add entrypoint
COPY *.sh /opt/app/onap/command-executor/

# add application
COPY @project.build.finalName@-@assembly.id@.tar.gz /source.tar.gz

RUN tar -xzf /source.tar.gz -C /tmp \
    && cp -rf /tmp/@project.build.finalName@/opt / \
    && rm -rf /source.tar.gz \
    && rm -rf /tmp/@project.build.finalName@ \
    && groupadd -r -g 1000 onap && useradd -r -m -d /home/onap -u 1000 -g onap onap \
    && mkdir -p /opt/app/onap/blueprints/deploy /opt/app/onap/logs \
    && touch /opt/app/onap/logs/application.log \
    && chown -R onap:onap /opt \
    && chmod -R 755 /opt \
    && apt-get update && apt-get install -y procps iputils-ping curl telnet && rm -rf /var/lib/apt/lists/* \
    && python -m pip install --no-cache-dir --upgrade pip setuptools \
    && pip install --no-cache-dir -r /opt/app/onap/python/requirements.txt

USER onap
ENTRYPOINT /opt/app/onap/command-executor/start.sh
