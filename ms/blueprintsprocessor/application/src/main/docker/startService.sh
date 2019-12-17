#!/bin/sh

nodeName=BlueprintsProcessor_1.0.0_$(cat /proc/self/cgroup | grep docker | sed s/\\//\\n/g | tail -1)

echo "${CLUSTER_ID}:${CLUSTER_NODE_ID} APP Config HOME : ${APP_CONFIG_HOME}"
export APP_HOME=/opt/app/onap

keytool -import -noprompt -trustcacerts -keystore $JAVA_HOME/jre/lib/security/cacerts -storepass changeit -alias ONAP -import -file $APP_CONFIG_HOME/ONAP_RootCA.cer

exec java -classpath "/etc:${APP_HOME}/lib/*:/lib/*:/src:/schema:/generated-sources:${APP_CONFIG_HOME}:${APP_HOME}" \
-DappName=${APPLICATIONNAME} -DappVersion=${BUNDLEVERSION} \
-DrouteOffer=${ROUTEOFFER} \
-DVERSION_ROUTEOFFER_ENVCONTEXT=${BUNDLEVERSION}/${STICKYSELECTORKEY}/${ENVCONTEXT} \
-DSecurityFilePath=/etc \
-DREST_NAME_NORMALIZER_PATTERN_FILE=/etc/PatternInputs.txt \
-Dms_name=org.onap.ccsdk.cds.blueprintsprocessor \
-Dlogging.config=${APP_CONFIG_HOME}/logback.xml \
-Djava.security.egd=file:/dev/./urandom \
-DAPPNAME=${APPLICATIONNAME} -DAPPENV=${APP_ENV} -DAPPVERSION=${APP_VERSION} -DNAMESPACE=${NAMESPACE} \
-Dspring.config.location=${APP_CONFIG_HOME}/ \
-DCLUSTER_ID=${CLUSTER_ID} \
-DCLUSTER_NODE_ID=${CLUSTER_NODE_ID} \
-DCLUSTER_NODE_ADDRESS=${CLUSTER_NODE_ID} \
-DCLUSTER_MEMBERS=${CLUSTER_MEMBERS} \
-DCLUSTER_STORAGE_PATH=${CLUSTER_STORAGE_PATH} \
-DCLUSTER_CONFIG_FILE=${CLUSTER_CONFIG_FILE} \
org.onap.ccsdk.cds.blueprintsprocessor.BlueprintProcessorApplicationKt
