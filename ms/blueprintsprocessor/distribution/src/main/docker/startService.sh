#!/bin/bash

nodeName=BlueprintsProcessor_1.0.0_$(cat /proc/self/cgroup | grep docker | sed s/\\//\\n/g | tail -1)

echo "APP Config HOME : ${APP_CONFIG_HOME}"
export APP_HOME=/opt/app/onap

keytool -import -trustcacerts -keystore $JAVA_HOME/jre/lib/security/cacerts -storepass changeit -alias ONAP -import -file $APP_CONFIG_HOME/ONAP_RootCA.cer

PASSWORD_OVERRIDES=""

if ! [ -z "${DB_PASSWORD+x}" ]; then
  PASSWORD_OVERRIDES="${PASSWORD_OVERRIDES} -Dblueprintsprocessor.db.primary.password=${DB_PASSWORD}"
fi

if ! [ -z "${USER_PASSWORD+x}" ]; then
  PASSWORD_OVERRIDES="${PASSWORD_OVERRIDES} -Dsecurity.user.password=${USER_PASSWORD}"
fi

if ! [ -z "${SDNCODL_PASSWORD+x}" ]; then
  PASSWORD_OVERRIDES="${PASSWORD_OVERRIDES} -Dblueprintsprocessor.restclient.sdncodl.password=${SDNCODL_PASSWORD}"
fi

if ! [ -z "${CONFIG_DATA_PASSWORD+x}" ]; then
  PASSWORD_OVERRIDES="${PASSWORD_OVERRIDES} -Dblueprintsprocessor.restclient.primary-config-data.password=${CONFIG_DATA_PASSWORD}"
fi

if ! [ -z "${AAI_DATA_PASSWORD+x}" ]; then
  PASSWORD_OVERRIDES="${PASSWORD_OVERRIDES} -Dblueprintsprocessor.restclient.primary-aai-data.password=${AAI_DATA_PASSWORD}"
fi

echo "PASSWORD_OVERRIDES: ${PASSWORD_OVERRIDES:-NONE}"

source /etc/run.source
