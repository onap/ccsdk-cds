/*
 * Copyright © 2019 Bell Canada.
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

package org.onap.ccsdk.cds.blueprintsprocessor.resource.api

import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.db.ResourceResolution
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.db.ResourceResolutionDBService
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintConstants
import org.onap.ccsdk.cds.controllerblueprints.core.asJsonPrimitive
import org.onap.ccsdk.cds.controllerblueprints.core.data.PropertyDefinition
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import org.onap.ccsdk.cds.controllerblueprints.resource.dict.ResourceAssignment
import org.onap.ccsdk.cds.error.catalog.core.ErrorPayload
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.reactive.server.WebTestClient

@RunWith(SpringRunner::class)
@WebFluxTest
@ContextConfiguration(
    classes = [
        TestDatabaseConfiguration::class, ErrorCatalogTestConfiguration::class,
        ResourceController::class, ResourceResolutionDBService::class
    ]
)
@ComponentScan(
    basePackages = [
        "org.onap.ccsdk.cds.controllerblueprints.core.service",
        "org.onap.ccsdk.cds.blueprintsprocessor.resource.api",
        "org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution"
    ]
)
@TestPropertySource(locations = ["classpath:application-test.properties"])
class ResourceControllerTest {

    private val log = LoggerFactory.getLogger(ResourceControllerTest::class.toString())

    @Autowired
    lateinit var resourceResolutionDBService: ResourceResolutionDBService

    @Autowired
    lateinit var webTestClient: WebTestClient

    val blueprintName = "baseconfiguration"
    val blueprintVersion = "1.0.0"
    val templatePrefix = "activate"

    @Test
    fun `ping return Success`() {
        runBlocking {
            webTestClient.get().uri("/api/v1/resources/health-check")
                .exchange()
                .expectStatus().isOk
                .expectBody()
                .equals("Success")
        }
    }

    @Test
    fun getAllFromResolutionKeyTest() {

        val resolutionKey = "1"
        val ra1 = createRA("bob")
        val ra2 = createRA("dylan")

        runBlocking {

            store(ra1, resKey = resolutionKey)
            store(ra2, resKey = resolutionKey)

            webTestClient
                .get()
                .uri("/api/v1/resources?bpName=$blueprintName&bpVersion=$blueprintVersion&artifactName=$templatePrefix&resolutionKey=$resolutionKey")
                .exchange()
                .expectStatus().isOk
                .expectBody()
                .consumeWith {
                    val json = String(it.responseBody!!)
                    val typeFactory = JacksonUtils.objectMapper.typeFactory
                    val list: List<ResourceResolution> = JacksonUtils.objectMapper.readValue(
                        json,
                        typeFactory.constructCollectionType(List::class.java, ResourceResolution::class.java)
                    )
                    Assert.assertEquals(2, list.size)
                    assertEqual(ra1, list[0])
                    assertEqual(ra1, list[0])
                }
        }
    }

    @Test
    fun getAllFromFromResourceTypeAndIdTest() {

        val resourceId = "1"
        val resourceType = "ServiceInstance"
        val ra1 = createRA("bob")
        val ra2 = createRA("dylan")

        runBlocking {

            store(ra1, resId = resourceId, resType = resourceType)
            store(ra2, resId = resourceId, resType = resourceType)

            webTestClient
                .get()
                .uri("/api/v1/resources?bpName=$blueprintName&bpVersion=$blueprintVersion&resourceType=$resourceType&resourceId=$resourceId")
                .exchange()
                .expectStatus().isOk
                .expectBody()
                .consumeWith {
                    val json = String(it.responseBody!!)
                    val typeFactory = JacksonUtils.objectMapper.typeFactory
                    val list: List<ResourceResolution> = JacksonUtils.objectMapper.readValue(
                        json,
                        typeFactory.constructCollectionType(List::class.java, ResourceResolution::class.java)
                    )
                    Assert.assertEquals(2, list.size)
                    assertEqual(ra1, list[0])
                    assertEqual(ra1, list[0])
                }
        }
    }

    @Test
    fun getAllFromMissingParamTest() {
        runBlocking {
            webTestClient
                .get()
                .uri("/api/v1/resources?bpName=$blueprintName&bpVersion=$blueprintVersion")
                .exchange()
                .expectStatus().is4xxClientError
                .expectBody()
                .consumeWith {
                    val r = JacksonUtils.objectMapper.readValue(it.responseBody, ErrorPayload::class.java)
                    Assert.assertEquals(
                        "Cause: Missing param. Either retrieve resolved value using artifact name and " +
                            "resolution-key OR using resource-id and resource-type. \n" +
                            " Action : Please verify your request.",
                        r.message
                    )
                }
        }
    }

    @Test
    fun getAllFromWrongInputTest() {
        runBlocking {
            webTestClient
                .get()
                .uri("/api/v1/resources?bpName=$blueprintName&bpVersion=$blueprintVersion&artifactName=$templatePrefix&resolutionKey=test&resourceId=1")
                .exchange()
                .expectStatus().is4xxClientError
                .expectBody()
                .consumeWith {
                    val r = JacksonUtils.objectMapper.readValue(it.responseBody, ErrorPayload::class.java)
                    Assert.assertEquals(
                        "Cause: Either retrieve resolved value using artifact name and resolution-key OR using " +
                            "resource-id and resource-type. \n Action : Please verify your request.",
                        r.message
                    )
                }
        }
    }

    @Test
    fun getOneFromResolutionKeyTest() {
        val resolutionKey = "3"
        val ra = createRA("joe")
        runBlocking {
            store(ra, resKey = resolutionKey)
        }
        runBlocking {
            webTestClient.get()
                .uri("/api/v1/resources/resource?bpName=$blueprintName&bpVersion=$blueprintVersion&artifactName=$templatePrefix&resolutionKey=$resolutionKey&name=joe")
                .exchange()
                .expectStatus().isOk
                .expectBody()
                .consumeWith {
                    val r = JacksonUtils.objectMapper.readValue(it.responseBody, ResourceResolution::class.java)
                    assertEqual(ra, r)
                }
        }
    }

    @Test
    fun getOneFromResolutionKey404Test() {
        val resolutionKey = "3"
        runBlocking {
            webTestClient.get()
                .uri("/api/v1/resources/resource?bpName=$blueprintName&bpVersion=$blueprintVersion&artifactName=$templatePrefix&resolutionKey=$resolutionKey&name=doesntexist")
                .exchange()
                .expectStatus().is4xxClientError
                .expectBody()
        }
    }

    private suspend fun store(
        resourceAssignment: ResourceAssignment,
        resKey: String = "",
        resId: String = "",
        resType: String = ""
    ) {
        resourceResolutionDBService.write(
            blueprintName,
            blueprintVersion,
            resKey,
            resId,
            resType,
            templatePrefix,
            resourceAssignment
        )
    }

    private fun createRA(prefix: String): ResourceAssignment {
        val property = PropertyDefinition()
        property.value = "value$prefix".asJsonPrimitive()

        val resourceAssignment = ResourceAssignment()
        resourceAssignment.name = prefix
        resourceAssignment.dictionaryName = "dd$prefix"
        resourceAssignment.dictionarySource = "source$prefix"
        resourceAssignment.version = 2
        resourceAssignment.status = BlueprintConstants.STATUS_SUCCESS
        resourceAssignment.property = property
        return resourceAssignment
    }

    private fun assertEqual(resourceAssignment: ResourceAssignment, resourceResolution: ResourceResolution) {
        Assert.assertEquals(
            JacksonUtils.getValue(resourceAssignment.property?.value!!).toString(),
            resourceResolution.value
        )
        Assert.assertEquals(resourceAssignment.status, resourceResolution.status)
        Assert.assertEquals(resourceAssignment.dictionarySource, resourceResolution.dictionarySource)
        Assert.assertEquals(resourceAssignment.dictionaryName, resourceResolution.dictionaryName)
        Assert.assertEquals(resourceAssignment.version, resourceResolution.dictionaryVersion)
        Assert.assertEquals(resourceAssignment.name, resourceResolution.name)
        Assert.assertEquals(blueprintVersion, resourceResolution.blueprintVersion)
        Assert.assertEquals(blueprintName, resourceResolution.blueprintName)
    }
}
