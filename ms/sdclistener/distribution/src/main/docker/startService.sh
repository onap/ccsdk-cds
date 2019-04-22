#!/bin/sh
echo "Starting SDC Listener"

java -classpath "/etc:${APP_HOME}/lib/*:/lib/*:/src:/schema:/generated-sources:${APP_CONFIG_HOME}:${APP_HOME}" \
-DappName=${APPLICATIONNAME} -DappVersion=${BUNDLEVERSION} \
-Dms_name=org.onap.ccsdk.cds.sdclistener\
-Dlogging.config=${APP_CONFIG_HOME}/logback.xml \
-Djava.security.egd=file:/dev/./urandom \
-DAPPNAME=${APP_NAME} -DAPPENV=${APP_ENV} -DAPPVERSION=${APP_VERSION} -DNAMESPACE=${NAMESPACE} \
-Dspring.config.location=${APP_CONFIG_HOME}/ \
org.onap.ccsdk.cds.sdclistener.SdcListenerApplication

