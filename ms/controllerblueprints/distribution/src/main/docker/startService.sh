#!/bin/bash

nodeName=ControllerBlueprints_1.0.0_$(cat /proc/self/cgroup | grep docker | sed s/\\//\\n/g | tail -1)

echo "APP Config HOME : ${APP_CONFIG_HOME}"
export APP_HOME=/opt/app/onap

PASSWORD_OVERRIDES=""

if ! [ -z "${AUTH_HASHED_PASSWORD+x}" ]; then
  PASSWORD_OVERRIDES="${PASSWORD_OVERRIDES} -Dbasic-auth.hashed-pwd=${AUTH_HASHED_PASSWORD}"
fi

if ! [ -z "${SPRING_DATASOURCE_PASSWORD+x}" ]; then
  PASSWORD_OVERRIDES="${PASSWORD_OVERRIDES} -Dspring.datasource.password=${SPRING_DATASOURCE_PASSWORD}"
fi

source /etc/run.source
