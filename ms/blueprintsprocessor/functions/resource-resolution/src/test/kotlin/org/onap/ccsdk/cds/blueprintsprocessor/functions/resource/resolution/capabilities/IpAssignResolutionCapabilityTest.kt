/*
 * Copyright © 2019 AT&T.
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

package org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.capabilities

import com.fasterxml.jackson.databind.node.NullNode
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.ResourceAssignmentRuntimeService
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.processor.CapabilityResourceResolutionProcessor
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.processor.ResourceAssignmentProcessor
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.resourceAssignments
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.resourceDefinitions
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.utils.ResourceAssignmentUtils
import org.onap.ccsdk.cds.blueprintsprocessor.rest.service.BluePrintRestLibPropertyService
import org.onap.ccsdk.cds.blueprintsprocessor.rest.service.BlueprintWebClientService
import org.onap.ccsdk.cds.blueprintsprocessor.services.execution.ComponentFunctionScriptingService
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintTypes
import org.onap.ccsdk.cds.controllerblueprints.core.asJsonPrimitive
import org.onap.ccsdk.cds.controllerblueprints.core.asJsonType
import org.onap.ccsdk.cds.controllerblueprints.core.logger
import org.onap.ccsdk.cds.controllerblueprints.core.service.BluePrintDependencyService
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import org.onap.ccsdk.cds.controllerblueprints.resource.dict.ResolutionSummary
import org.onap.ccsdk.cds.controllerblueprints.resource.dict.ResourceAssignment
import org.onap.ccsdk.cds.controllerblueprints.resource.dict.ResourceDefinition
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * @author saurav.paira
 */

class IpAssignResolutionCapabilityTest {
    val log = logger(IpAssignResolutionCapabilityTest::class)

    @Before
    fun setup() {

        mockkObject(BluePrintDependencyService)

        val blueprintWebClientService = mockk<BlueprintWebClientService>()
        // Create mock Response
        val mockResponse = BlueprintWebClientService.WebClientResponse(
            200,
            """{
            "fixed_ipv4_Address_01" : "10.10.10.11",
            "fixed_ipv4_Address_02" : "10.10.10.12",
            "fixed_ipv4_Address_03" : "10.10.10.13"
            }
        """.trimMargin()
        )
        every { blueprintWebClientService.exchangeResource(any(), any(), any()) } returns mockResponse

        val restLibPropertyService = mockk<BluePrintRestLibPropertyService>()
        every { restLibPropertyService.blueprintWebClientService("ipassign-ms") } returns blueprintWebClientService
        every { BluePrintDependencyService.applicationContext.getBean(any()) } returns restLibPropertyService
    }

    @Test
    fun testIpAssignResolutionCapability() {
        runBlocking {
            val componentFunctionScriptingService = mockk<ComponentFunctionScriptingService>()
            coEvery {
                componentFunctionScriptingService
                    .scriptInstance<ResourceAssignmentProcessor>(any(), any(), any())
            } returns IpAssignResolutionCapability()

            coEvery {
                componentFunctionScriptingService.cleanupInstance(any(), any())
            } returns mockk()

            val raRuntimeService = mockk<ResourceAssignmentRuntimeService>()
            every { raRuntimeService.bluePrintContext() } returns mockk()
            every { raRuntimeService.getInputValue("fixed_ipv4_Address_01") } returns NullNode.getInstance()
            every { raRuntimeService.getInputValue("fixed_ipv4_Address_02") } returns NullNode.getInstance()
            every { raRuntimeService.getInputValue("fixed_ipv4_Address_03") } returns NullNode.getInstance()

            every { raRuntimeService.getResolutionStore("CloudRegionId") } returns "cloud-123".asJsonPrimitive()
            every { raRuntimeService.getResolutionStore("IpServiceName") } returns "MobilityPlan".asJsonPrimitive()

            every { raRuntimeService.putResolutionStore(any(), any()) } returns Unit
            every { raRuntimeService.putDictionaryStore(any(), any()) } returns Unit

            val capabilityResourceResolutionProcessor =
                CapabilityResourceResolutionProcessor(componentFunctionScriptingService)
            capabilityResourceResolutionProcessor.raRuntimeService = raRuntimeService

            capabilityResourceResolutionProcessor.resourceDictionaries = resourceDefinitions()
            // log.info("ResourceAssignments Definitions : ${ capabilityResourceResolutionProcessor.resourceDictionaries.asJsonString(true)} ")
            val resourceAssignments = resourceAssignments()
            val resourceAssignmentList = resourceAssignments.values.toMutableList()
            // log.info("ResourceAssignments Assignments : ${resourceAssignmentList.asJsonString(true)} ")
            capabilityResourceResolutionProcessor.resourceAssignments = resourceAssignmentList

            var status = capabilityResourceResolutionProcessor.applyNB(resourceAssignments["fixed_ipv4_Address_01"]!!)
            assertTrue(status, "failed to execute capability source")
            assertEquals(
                "10.10.10.11".asJsonPrimitive(), resourceAssignments["fixed_ipv4_Address_01"]!!.property!!.value,
                "assigned value miss match"
            )

            status = capabilityResourceResolutionProcessor.applyNB(resourceAssignments["fixed_ipv4_Address_02"]!!)
            assertTrue(status, "failed to execute capability source")
            assertEquals(
                "10.10.10.12".asJsonPrimitive(), resourceAssignments["fixed_ipv4_Address_02"]!!.property!!.value,
                "assigned value miss match"
            )

            status = capabilityResourceResolutionProcessor.applyNB(resourceAssignments["fixed_ipv4_Address_03"]!!)
            assertTrue(status, "failed to execute capability source")
            assertEquals(
                "10.10.10.13".asJsonPrimitive(), resourceAssignments["fixed_ipv4_Address_03"]!!.property!!.value,
                "assigned value miss match"
            )

            val resoulutionSummary =
                ResourceAssignmentUtils.generateResolutionSummaryData(
                    resourceAssignments.values.toList(),
                    capabilityResourceResolutionProcessor.resourceDictionaries
                )
            log.info(resoulutionSummary.asJsonType().toPrettyString())
            assertNotNull(resoulutionSummary.asJsonType().get("resolution-summary"))

            val summaries = JacksonUtils.jsonNode(resoulutionSummary)["resolution-summary"]
            val list = JacksonUtils.getListFromJsonNode(summaries, ResolutionSummary::class.java)
            val ipAddress = list.filter { it.name == "fixed_ipv4_Address_01" }

            assertEquals(list.size, 5)
            assertNotNull(ipAddress[0].keyIdentifiers)
            assertNotNull(ipAddress[0].requestPayload)
        }
    }

    /** Test dictionaries */

    /** Test dictionaries */
    private fun resourceDefinitions(): MutableMap<String, ResourceDefinition> {
        return BluePrintTypes.resourceDefinitions {
            resourceDefinition("CloudRegionId", "Cloud Region Id Resource Definition") {
                tags("CloudRegionId")
                updatedBy("saurav.paira@att.com")
                property("string", true)
                sources {
                    sourceInput("input", "") {}
                }
            }
            resourceDefinition("IpServiceName", "Ip Service Name Resource Definition") {
                tags("IpServiceName")
                updatedBy("saurav.paira@att.com")
                property("string", true)
                sources {
                    sourceInput("input", "") {}
                }
            }
            resourceDefinition("fixed_ipv4_Address_01", "fixed_ipv4_Address_01 Resource Definition") {
                tags("fixed_ipv4_Address_01")
                updatedBy("saurav.paira@att.com")
                property("string", true)
                sources {
                    sourceCapability("ipassign-ms", "") {
                        definedProperties {
                            type("internal")
                            scriptClassReference(IpAssignResolutionCapability::class)
                            keyDependencies(
                                arrayListOf(
                                    "CloudRegionId",
                                    "IpServiceName"
                                )
                            )
                        }
                    }
                }
            }
            resourceDefinition("fixed_ipv4_Address_02", "fixed_ipv4_Address_02 Resource Definition") {
                tags("fixed_ipv4_Address_01")
                updatedBy("saurav.paira@att.com")
                property("string", true)
                sources {
                    sourceCapability("ipassign-ms", "") {
                        definedProperties {
                            type("internal")
                            scriptClassReference(IpAssignResolutionCapability::class)
                            keyDependencies(
                                arrayListOf(
                                    "CloudRegionId",
                                    "IpServiceName"
                                )
                            )
                        }
                    }
                }
            }
            resourceDefinition("fixed_ipv4_Address_03", "fixed_ipv4_Address_03 Resource Definition") {
                tags("fixed_ipv4_Address_03")
                updatedBy("saurav.paira@att.com")
                property("string", true)
                sources {
                    sourceCapability("ipassign-ms", "") {
                        definedProperties {
                            type("internal")
                            scriptClassReference(IpAssignResolutionCapability::class)
                            keyDependencies(
                                arrayListOf(
                                    "CloudRegionId",
                                    "IpServiceName"
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    private fun resourceAssignments(): MutableMap<String, ResourceAssignment> {
        return BluePrintTypes.resourceAssignments {
            resourceAssignment(
                name = "CloudRegionId", dictionaryName = "CloudRegionId",
                dictionarySource = "input"
            ) {
                property("string", true, "")
                dependencies(arrayListOf())
            }
            resourceAssignment(
                name = "IpServiceName", dictionaryName = "IpServiceName",
                dictionarySource = "input"
            ) {
                property("string", true, "")
                dependencies(arrayListOf())
            }
            resourceAssignment(
                name = "fixed_ipv4_Address_01", dictionaryName = "fixed_ipv4_Address_01",
                dictionarySource = "ipassign-ms"
            ) {
                property("string", true, "")
                dependencies(
                    arrayListOf(
                        "CloudRegionId",
                        "IpServiceName"
                    )
                )
            }
            resourceAssignment(
                name = "fixed_ipv4_Address_02", dictionaryName = "fixed_ipv4_Address_02",
                dictionarySource = "ipassign-ms"
            ) {
                property("string", true, "")
                dependencies(
                    arrayListOf(
                        "fixed_ipv4_Address_01"
                    )
                )
            }
            resourceAssignment(
                name = "fixed_ipv4_Address_03", dictionaryName = "fixed_ipv4_Address_03",
                dictionarySource = "ipassign-ms"
            ) {
                property("string", true, "")
                dependencies(
                    arrayListOf(
                        "fixed_ipv4_Address_02"
                    )
                )
            }
        }
    }
}
