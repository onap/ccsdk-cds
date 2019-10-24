/*
 * Copyright © 2019-2020 Orange.
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

package org.onap.ccsdk.cds.blueprintsprocessor.healthapi


import com.fasterxml.jackson.databind.node.ObjectNode
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.onap.ccsdk.cds.blueprintsprocessor.core.BluePrintCoreConfiguration
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ActionIdentifiers
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.CommonHeader
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ExecutionServiceInput
import org.onap.ccsdk.cds.blueprintsprocessor.healthapi.domain.HealthCheckStatus
import org.onap.ccsdk.cds.blueprintsprocessor.healthapi.service.health.BluePrintProcessorHealthCheck
import org.onap.ccsdk.cds.blueprintsprocessor.rest.service.BasicAuthRestClientService
import org.onap.ccsdk.cds.blueprintsprocessor.rest.service.BlueprintWebClientService
import org.onap.ccsdk.cds.blueprintsprocessor.services.execution.ComponentScriptExecutor
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BluePrintCatalogService
import org.onap.ccsdk.cds.controllerblueprints.core.service.BluePrintRuntimeService
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.security.SecurityProperties
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.http.HttpMethod
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.reactive.server.WebTestClient
import kotlin.test.assertEquals
import kotlin.test.assertNotNull


@RunWith(SpringRunner::class)
@WebFluxTest
@ContextConfiguration(classes = [BluePrintRuntimeService::class, BluePrintCoreConfiguration::class,
    BluePrintCatalogService::class, SecurityProperties::class, ComponentScriptExecutor::class])
@ComponentScan(basePackages = ["org.onap.ccsdk.cds.blueprintsprocessor", "org.onap.ccsdk.cds.controllerblueprints"])
@TestPropertySource(locations = ["classpath:application-test.properties"])
class HealthCheckApplicationTests {

    @Autowired
    lateinit var webTestClient: WebTestClient

    @Test
    fun testHealthApiUp() {
        val result = webTestClient.get().uri("/api/v1/combinedHealth")
                .exchange()
                .expectStatus().is2xxSuccessful
        println(result)
    }

    @Test
    fun testMetricsApiUp() {
        val result = webTestClient.get().uri("/api/v1/combinedMetrics")
                .exchange()
                .expectStatus().is2xxSuccessful

    }


}
