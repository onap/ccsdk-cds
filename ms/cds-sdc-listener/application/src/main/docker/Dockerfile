FROM openjdk:8-jdk-alpine

ENV HTTP_PROXY  ${HTTP_PROXY}
ENV HTTPS_PROXY ${HTTPS_PROXY}

RUN mkdir -p /opt/app/onap/cds-sdc-listener/
WORKDIR  /opt/app/onap/cds-sdc-listener/
COPY start.sh /opt/app/onap/cds-sdc-listener/
RUN chmod 751 /opt/app/onap/cds-sdc-listener/start.sh
COPY cds-sdc-listener-application-0.4.2-SNAPSHOT.jar /opt/app/onap/cds-sdc-listener/cds-sdc-listener-distribution.jar
EXPOSE 8000
CMD [ "/bin/sh", "/opt/app/onap/cds-sdc-listener/start.sh" ]