package cba.stream5

import com.fasterxml.jackson.databind.node.TextNode
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.processor.ResourceAssignmentProcessor
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.utils.ResourceAssignmentUtils
import org.onap.ccsdk.cds.blueprintsprocessor.rest.restClientService
import org.onap.ccsdk.cds.controllerblueprints.core.service.BluePrintDependencyService
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import org.onap.ccsdk.cds.controllerblueprints.resource.dict.ResourceAssignment
import org.slf4j.LoggerFactory
import org.springframework.http.HttpMethod

open class PnfIps : ResourceAssignmentProcessor() {

    private val log = LoggerFactory.getLogger(PnfIps::class.java)!!

    private val URI_PNF_BASE = "/aai/v14/network/pnfs/pnf/"


    override fun getName(): String {
        return "PnfIps"
    }

    override suspend fun processNB(resourceAssignment: ResourceAssignment) {
        log.info("Start resolving PnfIps")

        val ips = mutableListOf<String>()
        val list: List<String>
        val hostnames = raRuntimeService.getResolutionStore("hostnames")
        if (hostnames is TextNode) {
            list = JacksonUtils.getListFromJson(hostnames.asText(), String::class.java)
        } else {
            list = JacksonUtils.getListFromJsonNode(hostnames, String::class.java)
        }

        log.info("Hostname for which to retrieve IP: $list")

        val aaiClient = BluePrintDependencyService.restClientService("primary-aai-data")

        list.forEach {
            log.info("Retrieving $it information for inventory")
            val uri = URI_PNF_BASE + it
            val headers = mapOf(
                Pair("X-FromAppId", "AAI"),
                Pair("X-TransactionId", "get_pnf"))
            val res = aaiClient.exchangeNB(HttpMethod.GET.name, uri, "", headers)

            if (res.status != 200) {
                log.error("fail to get PNF information from AAI", res.body)
                ResourceAssignmentUtils.setFailedResourceDataValue(resourceAssignment, "Fail to resolve value")
                return
            } else {
                val ip = JacksonUtils.objectMapper.readTree(res.body)["ipaddress-v4-oam"].asText()
                log.info("Resolved IP Address for hostname($it) is: $ip")
                ips.add(ip)
            }
        }
        if (ips.isNotEmpty()) {
            ResourceAssignmentUtils.setResourceDataValue(resourceAssignment, raRuntimeService, ips)
        }
    }

    override suspend fun recoverNB(runtimeException: RuntimeException, resourceAssignment: ResourceAssignment) {
        log.info("Recovering input")
    }
}