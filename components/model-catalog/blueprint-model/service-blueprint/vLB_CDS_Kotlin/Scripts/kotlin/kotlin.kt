package org.onap.ccsdk.cds.blueprintsprocessor.functions.netconf.executor

import com.fasterxml.jackson.databind.node.ObjectNode
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ExecutionServiceInput
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.storedContentFromResolvedArtifactNB
import org.onap.ccsdk.cds.blueprintsprocessor.rest.BasicAuthRestClientProperties
import org.onap.ccsdk.cds.blueprintsprocessor.rest.RestClientProperties
import org.onap.ccsdk.cds.blueprintsprocessor.rest.service.BasicAuthRestClientService
import org.onap.ccsdk.cds.blueprintsprocessor.rest.service.BlueprintWebClientService
import org.onap.ccsdk.cds.blueprintsprocessor.services.execution.AbstractScriptComponentFunction
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import org.slf4j.LoggerFactory
import org.springframework.http.HttpMethod
import org.springframework.http.client.support.BasicAuthorizationInterceptor
import org.springframework.web.client.RestTemplate
import org.onap.ccsdk.cds.blueprintsprocessor.functions.netconf.executor.netconfClientService
import org.onap.ccsdk.cds.blueprintsprocessor.functions.netconf.executor.netconfDevice
import org.onap.ccsdk.cds.blueprintsprocessor.functions.netconf.executor.netconfDeviceInfo

open class ConfigDeploy : NetconfComponentFunction() {

    private val log = LoggerFactory.getLogger(ConfigDeploy::class.java)!!

    override suspend fun processNB(executionRequest: ExecutionServiceInput) {
        val resolution_key = getDynamicProperties("resolution-key").asText()
        log.info("resolution_key: $resolution_key")
        val payload = storedContentFromResolvedArtifactNB(resolution_key, "baseconfig")
        log.info("configuration: \n$payload")
        log.info("Waiting 1 minute and 30seconds or vLB to initialize ...")
        Thread.sleep(90000)
        val netconf_device = netconfDevice("netconf-connection")
        val netconf_rpc_client = netconf_device.netconfRpcService
        val netconf_session = netconf_device.netconfSession
        netconf_session.connect()
        netconf_rpc_client.lock("candidate")
        netconf_rpc_client.discardConfig()
        netconf_rpc_client.editConfig(payload, "candidate", "merge")
        netconf_rpc_client.commit()
        netconf_rpc_client.unLock("candidate")
        netconf_rpc_client.getConfig("", "running")

        //var payloadObject = JacksonUtils.jsonNode(payload) as ObjectNode
        //var vdns_ip: String = payloadObject.get("vdns-instance")[0].get("ip-addr").asText()
        netconf_session.disconnect()


    }

    override suspend fun recoverNB(runtimeException: RuntimeException, executionRequest: ExecutionServiceInput) {
        log.info("Executing Recovery")
    }
}
