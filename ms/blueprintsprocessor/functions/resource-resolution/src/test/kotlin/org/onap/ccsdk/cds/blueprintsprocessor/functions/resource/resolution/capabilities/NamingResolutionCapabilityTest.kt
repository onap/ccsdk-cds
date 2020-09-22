/*
 * Copyright © 2019 IBM.
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
import org.onap.ccsdk.cds.controllerblueprints.core.service.BluePrintContext
import org.onap.ccsdk.cds.controllerblueprints.core.service.BluePrintDependencyService
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import org.onap.ccsdk.cds.controllerblueprints.resource.dict.ResolutionSummary
import org.onap.ccsdk.cds.controllerblueprints.resource.dict.ResourceAssignment
import org.onap.ccsdk.cds.controllerblueprints.resource.dict.ResourceDefinition
import org.onap.ccsdk.cds.controllerblueprints.resource.dict.utils.BulkResourceSequencingUtils
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * @author brindasanth
 */

class NamingResolutionCapabilityTest {

    private val log = logger(NamingResolutionCapabilityTest::class)

    @Before
    fun setup() {

        mockkObject(BluePrintDependencyService)

        val blueprintWebClientService = mockk<BlueprintWebClientService>()
        // Create mock Response
        val mockResponse = BlueprintWebClientService.WebClientResponse<String>(
            200,
            """{
            "vf-module-name" : "dlsst001dbcx-adsf-Base-01",
            "vnfc-name" : "dlsst001dbcx"
            }
        """.trimMargin()
        )
        every { blueprintWebClientService.exchangeResource(any(), any(), any()) } returns mockResponse

        val restLibPropertyService = mockk<BluePrintRestLibPropertyService>()
        every { restLibPropertyService.blueprintWebClientService("naming-ms") } returns blueprintWebClientService
        every { BluePrintDependencyService.applicationContext.getBean(any()) } returns restLibPropertyService
    }

    @Test
    fun testNamingResolutionCapability() {
        runBlocking {
            val componentFunctionScriptingService = mockk<ComponentFunctionScriptingService>()
            coEvery {
                componentFunctionScriptingService
                    .scriptInstance<ResourceAssignmentProcessor>(any(), any(), any())
            } returns NamingResolutionCapability()

            coEvery {
                componentFunctionScriptingService.cleanupInstance(any(), any())
            } returns mockk()

            val raRuntimeService = mockk<ResourceAssignmentRuntimeService>()
            every { raRuntimeService.bluePrintContext() } returns mockk<BluePrintContext>()
            every { raRuntimeService.getInputValue("vf-module-name") } returns NullNode.getInstance()
            every { raRuntimeService.getInputValue("vnfc-name") } returns NullNode.getInstance()

            every { raRuntimeService.getResolutionStore("policy-instance-name") } returns "SDNC_Policy.Config_MS_1806SRIOV_VNATJson.4.xml".asJsonPrimitive()
            every { raRuntimeService.getResolutionStore("naming-code") } returns "dbc".asJsonPrimitive()
            every { raRuntimeService.getResolutionStore("vnf-name") } returns "vnf-123".asJsonPrimitive()
            every { raRuntimeService.getResolutionStore("vf-module-label") } returns "adsf".asJsonPrimitive()
            every { raRuntimeService.getResolutionStore("vf-module-type") } returns "base".asJsonPrimitive()
            every { raRuntimeService.getResolutionStore("cloud-region-id") } returns "region-123".asJsonPrimitive()

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

            val bulkSequenced =
                BulkResourceSequencingUtils.process(capabilityResourceResolutionProcessor.resourceAssignments)
            // log.info("Bulk Sequenced : ${bulkSequenced} ")
            val resourceAssignment1 = resourceAssignments["vf-module-name"]
            var status = capabilityResourceResolutionProcessor.applyNB(resourceAssignment1!!)
            assertTrue(status, "failed to execute capability source")
            assertEquals(
                "dlsst001dbcx-adsf-Base-01".asJsonPrimitive(), resourceAssignment1.property!!.value,
                "assigned value miss match"
            )

            val resourceAssignment2 = resourceAssignments["vnfc-name"]
            status = capabilityResourceResolutionProcessor.applyNB(resourceAssignment2!!)
            assertTrue(status, "failed to execute capability source")
            assertEquals(
                "dlsst001dbcx".asJsonPrimitive(), resourceAssignment2.property!!.value,
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
            val vnfModuleName = list.filter { it.name == "vf-module-name" }

            assertEquals(list.size, 9)
            assertNotNull(vnfModuleName[0].keyIdentifiers)
            assertNotNull(vnfModuleName[0].requestPayload)
        }
    }

    /** Test dictionaries */
    private fun resourceDefinitions(): MutableMap<String, ResourceDefinition> {
        return BluePrintTypes.resourceDefinitions {
            resourceDefinition("naming-code", "naming-code Resource Definition") {
                tags("naming-code")
                updatedBy("brindasanth@onap.com")
                property("string", true)
                sources {
                    sourceInput("input", "") {}
                }
            }
            resourceDefinition("naming-type", "naming-type Resource Definition") {
                tags("naming-type")
                updatedBy("brindasanth@onap.com")
                property("string", true)
                sources {
                    sourceInput("input", "") {}
                }
            }
            resourceDefinition("cloud-region-id", "cloud-region-id Resource Definition") {
                tags("cloud-region-id")
                updatedBy("brindasanth@onap.com")
                property("string", true)
                sources {
                    sourceInput("input", "") {}
                }
            }
            resourceDefinition("policy-instance-name", "policy-instance-name Resource Definition") {
                tags("policy-instance-name")
                updatedBy("sp694w@att.com")
                property("string", true)
                sources {
                    sourceInput("input", "") {}
                }
            }
            resourceDefinition("vnf-name", "vnf-name Resource Definition") {
                tags("vnf-name")
                updatedBy("sp694w@att.com")
                property("string", true)
                sources {
                    sourceInput("input", "") {}
                }
            }
            resourceDefinition("vf-module-label", "vf-module-label Resource Definition") {
                tags("vf-module-label")
                updatedBy("sp694w@att.com")
                property("string", true)
                sources {
                    sourceInput("input", "") {}
                }
            }
            resourceDefinition("vf-module-type", "vf-module-type Resource Definition") {
                tags("vf-module-type")
                updatedBy("sp694w@att.com")
                property("string", true)
                sources {
                    sourceInput("input", "") {}
                }
            }
            resourceDefinition("vf-module-name", "vf-module-name Resource Definition") {
                tags("vf-module-name")
                updatedBy("brindasanth@onap.com")
                property("string", true) {
                    metadata("naming-type", "VF-MODULE")
                }
                sources {
                    sourceCapability("naming-ms", "") {
                        definedProperties {
                            type("internal")
                            scriptClassReference(NamingResolutionCapability::class)
                            keyDependencies(
                                arrayListOf(
                                    "policy-instance-name",
                                    "naming-code",
                                    "vnf-name",
                                    "vf-module-label",
                                    "vf-module-type"
                                )
                            )
                        }
                    }
                }
            }
            resourceDefinition("vnfc-name", "vnfc-name Resource Definition") {
                tags("vnfc-name")
                updatedBy("brindasanth@onap.com")
                property("string", true) {
                    metadata("naming-type", "VNFC")
                }

                sources {
                    sourceCapability("naming-ms", "") {
                        definedProperties {
                            type("internal")
                            scriptClassReference(NamingResolutionCapability::class)
                            keyDependencies(
                                arrayListOf("vf-module-name")
                            )
                        }
                    }
                }
            }
            resourceDefinition("ra-dict-name-3", "ra-dict-name-3 Resource Definition") {
                tags("ra-dict-name-3")
                updatedBy("brindasanth@onap.com")
                property("string", true)
                sources {
                    sourceCapability("naming-ms", "") {
                        definedProperties {
                            type("internal")
                            scriptClassReference(NamingResolutionCapability::class)
                            keyDependencies(
                                arrayListOf("vf-module-name")
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
                name = "naming-code", dictionaryName = "naming-code",
                dictionarySource = "input"
            ) {
                property("string", true, "")
                dependencies(arrayListOf())
            }
            resourceAssignment(
                name = "naming-type", dictionaryName = "naming-type",
                dictionarySource = "input"
            ) {
                property("string", true, "")
                dependencies(arrayListOf())
            }
            resourceAssignment(
                name = "cloud-region-id", dictionaryName = " cloud-region-id",
                dictionarySource = "input"
            ) {
                property("string", true, "")
                dependencies(arrayListOf())
            }
            resourceAssignment(
                name = "policy-instance-name", dictionaryName = " policy-instance-name",
                dictionarySource = "input"
            ) {
                property("string", true, "")
                dependencies(arrayListOf())
            }
            resourceAssignment(
                name = "vnf-name", dictionaryName = " vnf-name",
                dictionarySource = "input"
            ) {
                property("string", true, "")
                dependencies(arrayListOf())
            }
            resourceAssignment(
                name = "vf-module-label", dictionaryName = " vf-module-label",
                dictionarySource = "input"
            ) {
                property("string", true, "")
                dependencies(arrayListOf())
            }
            resourceAssignment(
                name = "vf-module-type", dictionaryName = " vf-module-type",
                dictionarySource = "input"
            ) {
                property("string", true, "")
                dependencies(arrayListOf())
            }
            resourceAssignment(
                name = "vf-module-name", dictionaryName = "vf-module-name",
                dictionarySource = "naming-ms"
            ) {
                property("string", true, "")
                dependencies(
                    arrayListOf(
                        "policy-instance-name",
                        "naming-code",
                        "vnf-name",
                        "vf-module-label",
                        "vf-module-type"
                    )
                )
            }
            resourceAssignment(
                name = "vnfc-name", dictionaryName = "vnfc-name",
                dictionarySource = "naming-ms"
            ) {
                property("string", true, "")
                dependencies(
                    arrayListOf(
                        "vf-module-name"
                    )
                )
            }
        }
    }
}
