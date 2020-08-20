#!/bin/sh
echo "Starting SDC Listener"

export APP_HOME=/opt/app/onap
export APP_CONFIG_HOME=${APP_HOME}/config
echo "APP Config HOME : ${APP_CONFIG_HOME}"

java -classpath "/etc:${APP_HOME}/lib/*:/lib/*:/src:/schema:/generated-sources:${APP_CONFIG_HOME}:${APP_HOME}" \
-Dlogging.config=${APP_CONFIG_HOME}/logback.xml \
-Djava.security.egd=file:/dev/./urandom \
-Dspring.config.location=${APP_CONFIG_HOME}/ \
org.onap.ccsdk.cds.sdclistener.SdcListenerApplication
