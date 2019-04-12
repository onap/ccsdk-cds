#!/bin/sh
extraArgs=$@
java -jar /opt/app/onap/cds-sdc-listener-distribution.jar \
-Dspring.config=/opt/app/onap/config/application.conf \
-Djava.security.egd=file:/dev/./urandom \
${extraArgs}
