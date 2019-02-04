/*
 * ============LICENSE_START=======================================================
 * ONAP - CDS
 * ================================================================================
 * Copyright (C) 2019 Huawei Technologies Co., Ltd. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.ccsdk.apps.blueprintprocessor.dmaap

import org.junit.Test
import org.junit.runner.RunWith
import org.onap.ccsdk.apps.blueprintsprocessor.dmaap.DmaapEventPublisher
import org.onap.ccsdk.apps.blueprintsprocessor.dmaap.EnvironmentContext
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Unit test cases for DMaap publisher code.
 */
@RunWith(SpringRunner::class)
@EnableAutoConfiguration(exclude = [DataSourceAutoConfiguration::class])
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ContextConfiguration(classes = [EnvironmentContext::class, TestController::class,
    DmaapEventPublisher::class])
@TestPropertySource(properties = ["server.port=9111","aai.topic=cds_aai",
    "aai.username=admin","aai.password=admin","aai.host=127.0.0.1:9111",
    "mul.topic=cds_mul_1,cds_mul_2", "mul.username=admin","mul.password=admin",
    "mul.host=127.0.0.1:9111"])
class TestDmaapEventPublisher {

    /**
     * Tests the event properties being set properly and sent as request.
     */
    @Test
    fun testEventProperties() {
        val strList = mutableListOf<String>()
        val pub = DmaapEventPublisher(compName = "aai")
        strList.add("{\n" +
                "    \"a\" : \"hello\"\n" +
                "}")
        pub.sendMessage("1", strList)
        pub.close(2)
        pub.prodProps
        assertNotNull(pub.prodProps, "The property file updation failed")
        assertEquals(pub.prodProps.get("topic"), "cds_aai")
        assertEquals(pub.prodProps.get("username"), "admin")
        assertEquals(pub.prodProps.get("password"), "admin")
        assertEquals(pub.prodProps.get("host"), "127.0.0.1:9111")
    }

    /**
     * Tests the event properties with multiple topics.
     */
    @Test
    fun testMultiTopicProperties() {
        val strList = mutableListOf<String>()
        val pub = DmaapEventPublisher(compName = "mul")
        strList.add("{\n" +
                "    \"a\" : \"hello\"\n" +
                "}")
        pub.sendMessage("1", strList)
        pub.close(2)
        var tops = pub.topics
        assertNotNull(pub.prodProps, "The property file updation failed")
        assertEquals(tops[0], "cds_mul_1")
        assertEquals(tops[1], "cds_mul_2")
        //assertEquals(pub.topics.contains("cds_mul_2`"), true)
        assertEquals(pub.prodProps.get("username"), "admin")
        assertEquals(pub.prodProps.get("password"), "admin")
        assertEquals(pub.prodProps.get("host"), "127.0.0.1:9111")
    }
}

/**
 * Rest controller for testing the client request that is sent.
 */
@RestController
@RequestMapping(path = ["/events"])
open class TestController {

    /**
     * Accepts request for a topic and sends a message as response.
     */
    @PostMapping(path = ["/{topic}"])
    fun postTopic(@PathVariable(value = "topic") topic : String):
            ResponseEntity<Any> {
        var a = "{\n" +
                "    \"message\" : \"The message is published into $topic " +
                "topic\"\n" +
                "}"
        return ResponseEntity(a, HttpStatus.OK)
    }
}
