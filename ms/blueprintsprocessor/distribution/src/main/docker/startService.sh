#!/bin/sh

nodeName=BlueprintsProcessor_1.0.0_$(cat /proc/self/cgroup | grep docker | sed s/\\//\\n/g | tail -1)

echo "APP Config HOME : ${APP_CONFIG_HOME}"
export APP_HOME=/opt/app/onap

keytool -import -trustcacerts -keystore $JAVA_HOME/jre/lib/security/cacerts -storepass changeit -alias AAF -import -file $APP_CONFIG_HOME/AAF_RootCA.cer

source /etc/run.source
