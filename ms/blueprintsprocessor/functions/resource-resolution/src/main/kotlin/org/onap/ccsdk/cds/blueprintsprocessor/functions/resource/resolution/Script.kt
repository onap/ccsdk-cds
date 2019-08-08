package org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution

import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.processor.ResourceAssignmentProcessor
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.utils.ResourceAssignmentUtils
import org.onap.ccsdk.cds.controllerblueprints.resource.dict.ResourceAssignment
import org.slf4j.LoggerFactory
import org.springframework.http.HttpMethod

open class PnfIps : ResourceAssignmentProcessor() {

    private val log = LoggerFactory.getLogger(PnfIps::class.java)!!

    private val URI_PNF_BASE = "/aai/v11/network/pnfs/pnf/"


    override fun getName(): String {
        return "PnfIps"
    }

    override suspend fun processNB(resourceAssignment: ResourceAssignment) {
        log.info("Processing input")

        val ip = mutableListOf<String>()
        val hostnames = raRuntimeService.getJsonNodeFromResolutionStore("hostnames")

        val aaiClient = restClientService("primary-aai-data")

        hostnames.forEach {
            val uri = URI_PNF_BASE + it.asText()
            val headers = mapOf(
                Pair("Accept", "application/json"),
                Pair("Content-Type", "application/json"),
                Pair("X-FromAppId", "AAI"),
                Pair("X-TransactionId", "get_pnf"),
                Pair("Authorization", "Basic QUFJOkFBSQ=="))
            val res = aaiClient.exchangeNB(HttpMethod.GET.name, uri, "", headers)

            if (res.status != 200) {
                log.error("fail to get PNF information from AAI", res.body)
                ResourceAssignmentUtils.setFailedResourceDataValue(resourceAssignment, "Fail to resolve value")
                return
            } else {
                ip.add(res.body)
            }
        }
        ResourceAssignmentUtils.setResourceDataValue(resourceAssignment, raRuntimeService, ip)
    }


    override suspend fun recoverNB(runtimeException: RuntimeException, resourceAssignment: ResourceAssignment) {
        log.info("Recovering input")
    }
}