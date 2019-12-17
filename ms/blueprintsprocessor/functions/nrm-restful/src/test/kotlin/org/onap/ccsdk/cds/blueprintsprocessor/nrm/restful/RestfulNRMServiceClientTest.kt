/*
 *  Copyright © 2019 IBM.
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

package org.onap.ccsdk.cds.blueprintsprocessor.functions.nrm.restful

import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ExecutionServiceInput
import org.onap.ccsdk.cds.blueprintsprocessor.services.execution.AbstractScriptComponentFunction
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintException
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.ArrayNode
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import org.onap.ccsdk.cds.blueprintsprocessor.rest.RestLibConstants
import org.onap.ccsdk.cds.blueprintsprocessor.rest.service.BluePrintRestLibPropertyService
import org.onap.ccsdk.cds.blueprintsprocessor.rest.service.BlueprintWebClientService
import org.springframework.http.HttpMethod
import org.slf4j.LoggerFactory
import java.util.UUID
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.web.util.UriComponentsBuilder
import org.springframework.test.context.TestPropertySource
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@RunWith(SpringRunner::class)
@TestPropertySource(properties =
[
    "blueprintsprocessor.restclient.sample.type=basic-auth",
    "blueprintsprocessor.restclient.sample.url=http://127.0.0.1:8080",
    "blueprintsprocessor.restclient.sample.username=admin",
    "blueprintsprocessor.restclient.sample.password=admin"
    ])

class RestfulNRMServiceClientTest {

    @Autowired
    lateinit var bluePrintRestLibPropertyService: BluePrintRestLibPropertyService
    private val log = LoggerFactory.getLogger(RestfulNRMServiceClientTest::class.java)

    @Test
    fun TestCreateMOI() { 
        val httpmethodstr = "PUT"
        val classNameStr = "TestMangedObjectInstance"
        val idStr = generateMOIid()
        val pathStr = "/ProvisioningMnS/v1500"+"/"+classNameStr+"/"+idStr
        log.info("MOI Path: "+pathStr)
        var test_attributes_data = JacksonUtils.jsonNode("{}") as ObjectNode
        test_attributes_data.put("test_attribute_key", "test_attribute_value")
        var request_object_value = JacksonUtils.jsonNode("{}") as ObjectNode
        request_object_value.put("attributes", test_attributes_data)
        request_object_value.put("href", "/"+classNameStr+"/"+idStr)
        request_object_value.put("class", classNameStr)
        request_object_value.put("id", idStr)
        var request_object = JacksonUtils.jsonNode("{}") as ObjectNode
        request_object.put("data",request_object_value)
        val requestBodystr = request_object.toString()
        log.info("MOI request body: "+requestBodystr)
        val headers = mutableMapOf<String, String>()
        headers["Content-Type"] = "application/json"
        val web_client_service = bluePrintRestLibPropertyService.blueprintWebClientService("sample")
        val response = web_client_service.exchangeResource(HttpMethod.PUT.name, pathStr, requestBodystr)
        assertNotNull(response.body, "failed to get response")
        val mapper = ObjectMapper()
        val response_body: JsonNode = mapper.readTree(response.body)
        assertEquals(response_body, request_object)
         
    }
    
    fun TestGetMOIAttributes() {
        val httpmethodstr = "GET"
        val classNameStr = "TestMangedObjectInstance"
        val idStr = generateMOIid()
        var pathStr = "/ProvisioningMnS/v1500"+"/"+classNameStr+"/"+idStr
        pathStr = addQueryParameters(pathStr, "scope", "BASE_ONLY")
        pathStr = addQueryParameters(pathStr, "filter", "TestMangedObjectInstance")
        pathStr = addQueryParameters(pathStr, "fields", "test_attribute_key")

        log.info("MOI Path: "+pathStr)
        val web_client_service = bluePrintRestLibPropertyService.blueprintWebClientService("sample")
        val response = web_client_service.exchangeResource(HttpMethod.GET.name, pathStr, "")
        log.info("MOI response status: "+response.status)
        log.info("MOI response body: "+response.body)    
        assertNotNull(response.body, "failed to get response")
        val mapper = ObjectMapper()
        val response_body: JsonNode = mapper.readTree(response.body)
        assertEquals(response_body.get("data").get("attributes").get("test_attribute_key").toString(), "test_attribute_value")

    }

    fun TestModifyMOIAttributes() {
        val httpmethodstr = "PATCH"
        val classNameStr = "TestMangedObjectInstance"
        val idStr = generateMOIid()
        var pathStr = "/ProvisioningMnS/v1500"+"/"+classNameStr+"/"+idStr
        pathStr = addQueryParameters(pathStr, "scope", "BASE_ONLY")
        pathStr = addQueryParameters(pathStr, "filter", "TestMangedObjectInstance")           
        log.info("MOI Path: "+pathStr)
        var request_object_value = JacksonUtils.jsonNode("{}") as ObjectNode
        request_object_value.put("test_attribute_key", "modified_attribute_value")
        var request_object = JacksonUtils.jsonNode("{}") as ObjectNode
        request_object.put("data", request_object_value)
        val requestBodystr = request_object.toString()
        log.info("MOI request body: "+requestBodystr)
        val headers = mutableMapOf<String, String>()
        headers["Content-Type"] = "application/json"
        val web_client_service = bluePrintRestLibPropertyService.blueprintWebClientService("sample")
        val response = web_client_service.exchangeResource(HttpMethod.PATCH.name, pathStr, requestBodystr)
        log.info("MOI response status: "+response.status)
        log.info("MOI response body: "+response.body)
        assertNotNull(response.body, "failed to get response")
        val mapper = ObjectMapper()
        val response_body: JsonNode = mapper.readTree(response.body)
        assertEquals(response_body.get("data").get("attributes").get("test_attribute_key").toString(), "modified_attribute_value")
    }

    fun TestDeleteMOI() {
        val httpmethodstr = "DELETE"
        val classNameStr = "TestMangedObjectInstance"
        val idStr = generateMOIid()
        var pathStr = "/ProvisioningMnS/v1500"+"/"+classNameStr+"/"+idStr
        pathStr = addQueryParameters(pathStr, "scope", "BASE_ONLY")
        pathStr = addQueryParameters(pathStr, "filter", "TestMangedObjectInstance") 
        log.info("MOI Path: "+pathStr)
        val web_client_service = bluePrintRestLibPropertyService.blueprintWebClientService("sample")
        val response = web_client_service.exchangeResource(HttpMethod.DELETE.name, pathStr, "")
        log.info("MOI response status: "+response.status)
        log.info("MOI response body: "+response.body)
        assertNotNull(response.body, "failed to get response")    
        var test_attributes_data = JacksonUtils.jsonNode("{}") as ObjectNode
        test_attributes_data.put("test_attribute_key", "test_attribute_value")
        var request_object_value = JacksonUtils.jsonNode("{}") as ObjectNode
        request_object_value.put("attributes", test_attributes_data)
        request_object_value.put("href", "/"+classNameStr+"/"+idStr)
        request_object_value.put("class", classNameStr)
        request_object_value.put("id", idStr)
        var request_object = JacksonUtils.jsonNode("{}") as ObjectNode
        request_object.put("data",request_object_value)
        val mapper = ObjectMapper()
        val response_body: JsonNode = mapper.readTree(response.body)
        assertEquals(response_body, request_object)

    }

    fun generateMOIid():String{
        return UUID.randomUUID().toString().replace("-","") 
    }

    fun addQueryParameters(old_uri:String, key:String, value:String):String{
        return UriComponentsBuilder.fromUriString(old_uri).queryParam(key, value).build().toString() 
    }


}