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

import com.fasterxml.jackson.databind.node.ObjectNode
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.onap.ccsdk.cds.blueprintsprocessor.rest.BlueprintRestLibConfiguration
import org.onap.ccsdk.cds.blueprintsprocessor.rest.service.BlueprintRestLibPropertyService
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@RunWith(SpringRunner::class)
@EnableAutoConfiguration(exclude = [DataSourceAutoConfiguration::class])
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ContextConfiguration(classes = [BlueprintRestLibConfiguration::class, NrmTestController::class])
@TestPropertySource(
    properties = [
        "blueprintsprocessor.restclient.nrm.type=basic-auth",
        "blueprintsprocessor.restclient.nrm.url=http://127.0.0.1:8080",
        "blueprintsprocessor.restclient.nrm.username=admin",
        "blueprintsprocessor.restclient.nrm.password=admin"
    ]
)

@Ignore
class RestfulNRMServiceClientTest {

    @Autowired
    lateinit var restfulNRMServiceClient: RestfulNRMServiceClient
    lateinit var bluePrintRestLibPropertyService: BlueprintRestLibPropertyService

    @Test
    fun testCreateMOI() {
        val restClientService = bluePrintRestLibPropertyService.blueprintWebClientService("nrm")
        val idStr = restfulNRMServiceClient.generateMOIid()
        var test_moi_data = JacksonUtils.jsonNode("{}") as ObjectNode
        test_moi_data.put("className", "TestMangedObjectInstance")
        var test_attributes_data = JacksonUtils.jsonNode("{}") as ObjectNode
        test_attributes_data.put("test_attribute_key", "test_attribute_value")
        test_moi_data.put("data", test_attributes_data)
        val response = restfulNRMServiceClient.createMOI(restClientService, idStr, test_moi_data)
        assertNotNull(response, "failed to get createMOI response")
        assertEquals("Create MOI object successfully", response.get("body").get("data").toString(), "failed to get createMOI response")
    }

    @Test
    fun testGetMOIAttributes() {
        val restClientService = bluePrintRestLibPropertyService.blueprintWebClientService("nrm")
        val idStr = restfulNRMServiceClient.generateMOIid()
        var test_moi_data = JacksonUtils.jsonNode("{}") as ObjectNode
        test_moi_data.put("className", "TestMangedObjectInstance")
        test_moi_data.put("scope", "BASE_ONLY")
        test_moi_data.put("filter", "TestMangedObjectInstance")
        test_moi_data.put("fields", "test_attribute_key")
        val response = restfulNRMServiceClient.getMOIAttributes(restClientService, idStr, test_moi_data)
        assertNotNull(response, "failed to get getMOIAttributes response")
        assertEquals("Get MOI object attributes successfully", response.get("body").get("data").toString(), "failed to get getMOIAttributes response")
    }

    @Test
    fun testModifyMOIAttributes() {
        val restClientService = bluePrintRestLibPropertyService.blueprintWebClientService("nrm")
        val idStr = restfulNRMServiceClient.generateMOIid()
        var test_moi_data = JacksonUtils.jsonNode("{}") as ObjectNode
        test_moi_data.put("className", "TestMangedObjectInstance")
        test_moi_data.put("scope", "BASE_ONLY")
        test_moi_data.put("filter", "TestMangedObjectInstance")
        var test_attributes_data = JacksonUtils.jsonNode("{}") as ObjectNode
        test_attributes_data.put("test_attribute_key", "modified_attribute_value")
        test_moi_data.put("data", test_attributes_data)
        val response = restfulNRMServiceClient.modifyMOIAttributes(restClientService, idStr, test_moi_data)
        assertNotNull(response, "failed to get modifyMOIAttributes response")
        assertEquals(
            "Modify MOI object attributes successfully",
            response.get("body").get("data").toString(),
            "failed to get modifyMOIAttributes response"
        )
    }

    @Test
    fun testDeleteMOI() {
        val restClientService = bluePrintRestLibPropertyService.blueprintWebClientService("nrm")
        val idStr = restfulNRMServiceClient.generateMOIid()
        var test_moi_data = JacksonUtils.jsonNode("{}") as ObjectNode
        test_moi_data.put("className", "TestMangedObjectInstance")
        test_moi_data.put("scope", "BASE_ONLY")
        test_moi_data.put("filter", "TestMangedObjectInstance")
        val response = restfulNRMServiceClient.deleteMOI(restClientService, idStr, test_moi_data)
        assertNotNull(response, "failed to get delete response")
        assertEquals("Delete MOI object attributes successfully", response.get("body").get("data").toString(), "failed to get delete response")
    }
}

/**
 * Sample controller code for testing the above four functions.
 */
@RestController
@RequestMapping("/ProvisioningMnS/v1500")
open class NrmTestController {

    @PutMapping("/TestMangedObjectInstance")
    fun putMOI(): ResponseEntity<Any> {
        var a = "{\n" + "\"data\" : \"Create MOI object successfully" + "}"
        return ResponseEntity(a, HttpStatus.OK)
    }

    @GetMapping("/TestMangedObjectInstance")
    fun getMOI(): ResponseEntity<Any> {
        var a = "{\n" + "\"data\" : \"Get MOI object attributes successfully" + "}"
        return ResponseEntity(a, HttpStatus.OK)
    }

    @PatchMapping("/TestMangedObjectInstance")
    fun patchMOI(): ResponseEntity<Any> {
        var a = "{\n" + "\"data\" : \"Modify MOI object attributes successfully" + "}"
        return ResponseEntity(a, HttpStatus.OK)
    }

    @DeleteMapping("/TestMangedObjectInstance")
    fun deleteMOI(): ResponseEntity<Any> {
        var a = "{\n" + "\"data\" : \"Delete MOI object attributes successfully" + "}"
        return ResponseEntity(a, HttpStatus.OK)
    }
}
