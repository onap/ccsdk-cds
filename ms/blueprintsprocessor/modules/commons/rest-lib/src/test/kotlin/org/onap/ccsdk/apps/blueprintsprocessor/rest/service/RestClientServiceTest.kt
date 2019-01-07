/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
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

package org.onap.ccsdk.apps.blueprintsprocessor.rest.service

import org.junit.runner.RunWith
import org.onap.ccsdk.apps.blueprintsprocessor.core.BluePrintProperties
import org.onap.ccsdk.apps.blueprintsprocessor.core.BlueprintPropertyConfiguration
import org.onap.ccsdk.apps.blueprintsprocessor.rest.BluePrintRestLibConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import kotlin.test.Test
import kotlin.test.assertNotNull

@RunWith(SpringRunner::class)
@EnableAutoConfiguration(exclude = [DataSourceAutoConfiguration::class])
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ContextConfiguration(classes = [BluePrintRestLibConfiguration::class, BlueprintPropertyConfiguration::class,
    SampleController::class, BluePrintProperties::class])
@TestPropertySource(properties =
["server.port=9111",
    "blueprintsprocessor.restclient.sample.type=basic-auth",
    "blueprintsprocessor.restclient.sample.url=http://127.0.0.1:9111",
    "blueprintsprocessor.restclient.sample.userId=sampleuser",
    "blueprintsprocessor.restclient.sample.token=sampletoken"])
class RestClientServiceTest {

    @Autowired
    lateinit var bluePrintRestLibPropertyService: BluePrintRestLibPropertyService

    @Test
    fun testBaseAuth() {

        val restClientService = bluePrintRestLibPropertyService.blueprintWebClientService("sample")
        val headers = mutableMapOf<String, String>()
        headers["X-Transaction-Id"] = "1234"
        val response = restClientService.getResource("/sample/name", headers, String::class.java)
        assertNotNull(response, "failed to get response")
    }

}

@RestController
@RequestMapping("/sample")
open class SampleController {
    @GetMapping("/name")
    fun getName(): String = "Sample Controller"
}

