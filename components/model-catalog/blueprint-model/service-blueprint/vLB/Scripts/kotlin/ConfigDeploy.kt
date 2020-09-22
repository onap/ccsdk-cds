/*
 * Copyright © 2019 IBM, Bell Canada, Orange
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onap.ccsdk.cds.blueprintsprocessor.services.execution.scripts

import com.fasterxml.jackson.databind.node.ObjectNode
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ExecutionServiceInput
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.storedContentFromResolvedArtifactNB
import org.onap.ccsdk.cds.blueprintsprocessor.rest.BasicAuthRestClientProperties
import org.onap.ccsdk.cds.blueprintsprocessor.rest.service.BasicAuthRestClientService
import org.onap.ccsdk.cds.blueprintsprocessor.rest.service.BlueprintWebClientService
import org.onap.ccsdk.cds.blueprintsprocessor.services.execution.AbstractScriptComponentFunction
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import org.slf4j.LoggerFactory
import org.springframework.http.HttpMethod
import org.springframework.web.client.RestTemplate
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintProcessorException

open class ConfigDeploy : AbstractScriptComponentFunction() {

    private val log = LoggerFactory.getLogger(ConfigDeploy::class.java)!!

    override fun getName(): String {
        return "Check"
    }

    override suspend fun processNB(executionRequest: ExecutionServiceInput) {
        log.info("executing script")
        val resolution_key = getDynamicProperties("resolution-key").asText()
        log.info("resolution_key: $resolution_key")

        val payload = storedContentFromResolvedArtifactNB(resolution_key, "baseconfig")
        log.info("configuration: $payload")

        val payloadObject = JacksonUtils.jsonNode(payload) as ObjectNode
        val vdns_ip: String = payloadObject.get("vdns-instance")[0].get("ip-addr").asText()

        val blueprintContext = bluePrintRuntimeService.bluePrintContext()
        val requirement = blueprintContext.nodeTemplateRequirement(nodeTemplateName, "restconf-connection")
        val capabilityProperties = bluePrintRuntimeService.resolveNodeTemplateCapabilityProperties(requirement.node!!, requirement.capability!!)
        val netconfDeviceInfo = JacksonUtils.getInstanceFromMap(capabilityProperties, NetconfDeviceInfo::class.java)
        log.info("Waiting for 2 minutes until vLB intializes ...")
        // Thread.sleep(120000)
        val uri =
            "http://${netconfDeviceInfo.ipAddress}:8183/restconf/config/vlb-business-vnf-onap-plugin:vlb-business-vnf-onap-plugin/vdns-instances/vdns-instance/$vdns_ip"
        val restTemplate = RestTemplate()
        val mapOfHeaders = hashMapOf<String, String>()
        mapOfHeaders.put("Accept", "application/json")
        mapOfHeaders.put("Content-Type", "application/json")
        mapOfHeaders.put("cache-control", " no-cache")
        mapOfHeaders.put("Accept", "application/json")
        val basicAuthRestClientProperties: BasicAuthRestClientProperties = BasicAuthRestClientProperties()
        basicAuthRestClientProperties.username = "admin"
        basicAuthRestClientProperties.password = "admin"
        basicAuthRestClientProperties.url = uri
        basicAuthRestClientProperties.additionalHeaders = mapOfHeaders
        val basicAuthRestClientService: BasicAuthRestClientService = BasicAuthRestClientService(basicAuthRestClientProperties)
        try {
            val result: BlueprintWebClientService.WebClientResponse<String> =
                basicAuthRestClientService.exchangeResource(HttpMethod.PUT.name, "", payload)
            print(result)
            basicAuthRestClientProperties.url =
                "http://${netconfDeviceInfo.ipAddress}:8183/restconf/config/vlb-business-vnf-onap-plugin:vlb-business-vnf-onap-plugin/vdns-instances"
            val resultOfGet: BlueprintWebClientService.WebClientResponse<String> =
                basicAuthRestClientService.exchangeResource(HttpMethod.GET.name, "", "")
            print(resultOfGet)
        } catch (e: Exception) {
            log.info("Caught exception trying to connect to vLB!!")
            throw BluePrintProcessorException("${e.message}")
        }
    }

    override suspend fun recoverNB(runtimeException: RuntimeException, executionRequest: ExecutionServiceInput) {
        log.info("Executing Recovery")
        bluePrintRuntimeService.getBluePrintError().addError("${runtimeException.message}")
    }
}

class NetconfDeviceInfo {

    @get:JsonProperty("login-account")
    var username: String? = null

    @get:JsonProperty("login-key")
    var password: String? = null

    @get:JsonProperty("target-ip-address")
    var ipAddress: String? = null

    @get:JsonProperty("port-number")
    var port: Int = 0

    @get:JsonProperty("connection-time-out")
    var connectTimeout: Long = 5

    @get:JsonIgnore
    var source: String? = null

    @get:JsonIgnore
    var replyTimeout: Int = 5

    @get:JsonIgnore
    var idleTimeout: Int = 99999

    override fun toString(): String {
        return "$ipAddress:$port"
    }

    // TODO: should this be a data class instead? Is anything using the JSON serdes?
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        return true
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }
}
