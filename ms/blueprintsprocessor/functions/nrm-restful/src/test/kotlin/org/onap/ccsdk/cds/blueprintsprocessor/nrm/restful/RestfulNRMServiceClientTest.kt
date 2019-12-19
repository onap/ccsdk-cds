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

import com.fasterxml.jackson.databind.node.ObjectNode
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import org.slf4j.LoggerFactory
import java.util.UUID
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.web.util.UriComponentsBuilder

@RunWith(SpringRunner::class)

class RestfulNRMServiceClientTest {

    private val log = LoggerFactory.getLogger(RestfulNRMServiceClientTest::class.java)
    @Test
    fun TestCreateMOI() {
        val httpmethodstr = "PUT"
        val classNameStr = "TestMangedObjectInstance"
        val idStr = generateMOIid()
        val pathStr = "/ProvisioningMnS/v1500" + "/" + classNameStr + "/" + idStr
        log.info("MOI Path: " + pathStr)
        var test_attributes_data = JacksonUtils.jsonNode("{}") as ObjectNode
        test_attributes_data.put("test_attribute_key", "test_attribute_value")
        var request_object_value = JacksonUtils.jsonNode("{}") as ObjectNode
        request_object_value.put("attributes", test_attributes_data)
        request_object_value.put("href", "/" + classNameStr + "/" + idStr)
        request_object_value.put("class", classNameStr)
        request_object_value.put("id", idStr)
        var request_object = JacksonUtils.jsonNode("{}") as ObjectNode
        request_object.put("data", request_object_value)
        val requestBodystr = request_object.toString()
        log.info("MOI request body: " + requestBodystr)
    }

    fun TestGetMOIAttributes() {
        val httpmethodstr = "GET"
        val classNameStr = "TestMangedObjectInstance"
        val idStr = generateMOIid()
        var pathStr = "/ProvisioningMnS/v1500" + "/" + classNameStr + "/" + idStr
        pathStr = addQueryParameters(pathStr, "scope", "BASE_ONLY")
        pathStr = addQueryParameters(pathStr, "filter", "TestMangedObjectInstance")
        pathStr = addQueryParameters(pathStr, "fields", "test_attribute_key")
        log.info("MOI Path: " + pathStr)
    }

    fun TestModifyMOIAttributes() {
        val httpmethodstr = "PATCH"
        val classNameStr = "TestMangedObjectInstance"
        val idStr = generateMOIid()
        var pathStr = "/ProvisioningMnS/v1500" + "/" + classNameStr + "/" + idStr
        pathStr = addQueryParameters(pathStr, "scope", "BASE_ONLY")
        pathStr = addQueryParameters(pathStr, "filter", "TestMangedObjectInstance")
        log.info("MOI Path: " + pathStr)
        var request_object_value = JacksonUtils.jsonNode("{}") as ObjectNode
        request_object_value.put("test_attribute_key", "modified_attribute_value")
        var request_object = JacksonUtils.jsonNode("{}") as ObjectNode
        request_object.put("data", request_object_value)
        val requestBodystr = request_object.toString()
        log.info("MOI request body: " + requestBodystr)
    }

    fun TestDeleteMOI() {
        val httpmethodstr = "DELETE"
        val classNameStr = "TestMangedObjectInstance"
        val idStr = generateMOIid()
        var pathStr = "/ProvisioningMnS/v1500" + "/" + classNameStr + "/" + idStr
        pathStr = addQueryParameters(pathStr, "scope", "BASE_ONLY")
        pathStr = addQueryParameters(pathStr, "filter", "TestMangedObjectInstance")
        log.info("MOI Path: " + pathStr)
    }

    fun generateMOIid(): String {
        return UUID.randomUUID().toString().replace("-", "")
    }

    fun addQueryParameters(old_uri: String, key: String, value: String): String {
        return UriComponentsBuilder.fromUriString(old_uri).queryParam(key, value).build().toString()
    }
}
