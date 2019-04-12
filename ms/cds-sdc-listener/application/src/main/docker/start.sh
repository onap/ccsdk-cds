#!/bin/sh
extraArgs=$@
java -classpath /opt/app/onap/cds-sdc-listener/cds-sdc-listener-distribution.jar \
-Djava.security.egd=file:/dev/./urandom \
-Dlistenerservice.config.asdcAddress=${asdcAddress} \
-Dlistenerservice.config.messageBusAddress=${messageBusAddress} \
-Dlistenerservice.config.user=${user} \
-Dlistenerservice.config.password=${password} \
-Dlistenerservice.config.pollingInterval=${pollingInterval} \
-Dlistenerservice.config.pollingTimeout=${pollingTimeout} \
-Dlistenerservice.config.relevantArtifactTypes=${relevantArtifactTypes} \
-Dlistenerservice.config.consumerGroup=${consumerGroup} \
-Dlistenerservice.config.environmentName=${environmentName} \
-Dlistenerservice.config.consumerId=${consumerId} \
-Dlistenerservice.config.keyStorePassword=${keyStorePassword} \
-Dlistenerservice.config.keyStorePath=${keyStorePath} \
-Dlistenerservice.config.activateServerTLSAuth=${activateServerTLSAuth} \
-Dlistenerservice.config.isUseHttpsWithDmaap=${isUseHttpsWithDmaap} \
-Dlistenerservice.config.archivePath=${archivePath} \
-Dlistenerservice.config.grpcAddress=${grpcAddress} \
-Dlistenerservice.config.grpcPort=${grpcPort} \
-Dlistenerservice.config.authHeader=${authHeader} \
org.onap.ccsdk.cds.cdssdclistener.CdsSdcListenerApplication \
${extraArgs}
