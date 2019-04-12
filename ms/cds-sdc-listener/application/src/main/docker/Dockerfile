FROM openjdk:8-jdk-alpine

ENV HTTP_PROXY  ${HTTP_PROXY}
ENV HTTPS_PROXY ${HTTPS_PROXY}

RUN mkdir -p /opt/app/onap/ /opt/app/onap/config
WORKDIR  /opt/app/onap/
COPY start.sh /opt/app/onap/
COPY application.yml /opt/app/onap/config
RUN chmod 751 /opt/app/onap/start.sh
COPY cds-sdc-listener-application-0.4.2-SNAPSHOT.jar /opt/app/onap/cds-sdc-listener-distribution.jar
EXPOSE 9000
ENTRYPOINT /opt/app/onap/start.sh
