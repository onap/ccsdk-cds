/*
 *  Copyright © 2019 Huawei.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.onap.ccsdk.cds.blueprintsprocessor.functions.restful.executor.nrmfunction

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import org.onap.ccsdk.cds.blueprintsprocessor.rest.service.BlueprintWebClientService
import org.onap.ccsdk.cds.controllerblueprints.core.logger
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import org.springframework.http.HttpMethod
import org.springframework.web.util.UriComponentsBuilder
import java.util.UUID

class RestfulNRMServiceClient() {

    private val log = logger(RestfulNRMServiceClient::class.java)

    fun createMOI(web_client_service: BlueprintWebClientService, idStr: String, managed_object_instance: JsonNode): ObjectNode {
        val classNameStr = managed_object_instance.get("className").toString().replace("\"", "")
        val pathStr = "/ProvisioningMnS/v1500/$classNameStr/$idStr"
        log.info("MOI Path: " + pathStr)
        var request_object_value = JacksonUtils.jsonNode("{}") as ObjectNode
        request_object_value.put("attributes", managed_object_instance.get("data"))
        request_object_value.put("href", "/" + classNameStr + "/" + idStr)
        request_object_value.put("class", classNameStr)
        request_object_value.put("id", idStr)
        var request_object = JacksonUtils.jsonNode("{}") as ObjectNode
        request_object.put("data", request_object_value)
        val requestBodystr = request_object.toString()
        log.info("MOI request body: " + requestBodystr)
        val response = web_client_service.exchangeResource(HttpMethod.PUT.name, pathStr, requestBodystr)
        var response_object = generateResponse(response.status, response.body)
        log.info("MOI response status: " + response.status)
        return response_object
    }

    fun getMOIAttributes(web_client_service: BlueprintWebClientService, idStr: String, managed_object_instance: JsonNode): ObjectNode {
        val classNameStr = managed_object_instance.get("className").toString().replace("\"", "")
        var pathStr = "/ProvisioningMnS/v1500/$classNameStr/$idStr"
        pathStr = addQueryParameters(pathStr, "scope", managed_object_instance.get("scope").toString().replace("\"", ""))
        pathStr = addQueryParameters(pathStr, "filter", managed_object_instance.get("filter").toString().replace("\"", ""))
        for (attribute_value in managed_object_instance.get("fields")) {
            pathStr = addQueryParameters(pathStr, "fields", attribute_value.toString().replace("\"", ""))
        }
        log.info("MOI Path: " + pathStr)
        val response = web_client_service.exchangeResource(HttpMethod.GET.name, pathStr, "")
        log.info("MOI response status: " + response.status)
        var response_object = generateResponse(response.status, response.body)
        return response_object
    }

    fun modifyMOIAttributes(web_client_service: BlueprintWebClientService, idStr: String, managed_object_instance: JsonNode): ObjectNode {
        val classNameStr = managed_object_instance.get("className").toString().replace("\"", "")
        var pathStr = "/ProvisioningMnS/v1500/$classNameStr/$idStr"
        pathStr = addQueryParameters(pathStr, "scope", managed_object_instance.get("scope").toString().replace("\"", ""))
        pathStr = addQueryParameters(pathStr, "filter", managed_object_instance.get("filter").toString().replace("\"", ""))
        log.info("MOI Path: " + pathStr)
        var request_object = JacksonUtils.jsonNode("{}") as ObjectNode
        request_object.put("data", managed_object_instance.get("data"))
        val requestBodystr = request_object.toString()
        log.info("MOI request body: " + requestBodystr)
        val response = web_client_service.exchangeResource(HttpMethod.PATCH.name, pathStr, requestBodystr)
        log.info("MOI response status: " + response.status)
        var response_object = generateResponse(response.status, response.body)
        return response_object
    }

    fun deleteMOI(web_client_service: BlueprintWebClientService, idStr: String, managed_object_instance: JsonNode): ObjectNode {
        val classNameStr = managed_object_instance.get("className").toString().replace("\"", "")
        var pathStr = "/ProvisioningMnS/v1500/$classNameStr/$idStr"
        pathStr = addQueryParameters(pathStr, "scope", managed_object_instance.get("scope").toString().replace("\"", ""))
        pathStr = addQueryParameters(pathStr, "filter", managed_object_instance.get("filter").toString().replace("\"", ""))
        log.info("MOI Path: " + pathStr)
        val response = web_client_service.exchangeResource(HttpMethod.DELETE.name, pathStr, "")
        log.info("MOI response status: " + response.status)
        var response_object = generateResponse(response.status, response.body)
        return response_object
    }

    fun generateResponse(status: Int, body: String): ObjectNode {
        var response_object = JacksonUtils.jsonNode("{}") as ObjectNode
        response_object.put("status", status)
        val mapper = ObjectMapper()
        val response_body: JsonNode = mapper.readTree(body)
        response_object.put("body", response_body)
        return response_object
    }

    fun generateMOIid(): String {
        return UUID.randomUUID().toString()
    }

    fun addQueryParameters(old_uri: String, key: String, value: String): String {
        return UriComponentsBuilder.fromUriString(old_uri).queryParam(key, value).build().toString()
    }
}
