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
import org.onap.ccsdk.cds.blueprintsprocessor.rest.service.BluePrintRestLibPropertyService
import org.onap.ccsdk.cds.blueprintsprocessor.rest.service.BlueprintWebClientService
import org.onap.ccsdk.cds.blueprintsprocessor.services.execution.ComponentFunctionScriptingService
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintTypes
import org.onap.ccsdk.cds.controllerblueprints.core.asJsonPrimitive
import org.onap.ccsdk.cds.controllerblueprints.core.logger
import org.onap.ccsdk.cds.controllerblueprints.core.service.BluePrintContext
import org.onap.ccsdk.cds.controllerblueprints.core.service.BluePrintDependencyService
import org.onap.ccsdk.cds.controllerblueprints.resource.dict.ResourceAssignment
import org.onap.ccsdk.cds.controllerblueprints.resource.dict.ResourceDefinition
import org.onap.ccsdk.cds.controllerblueprints.resource.dict.utils.BulkResourceSequencingUtils
import kotlin.test.Test
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
            200, """{
            "ip-address-1" : "127.0.0.1",
            "ip-address-2" : "127.0.0.2",
            "ip-address-3" : "127.0.0.3"
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
            every { raRuntimeService.getInputValue("ra-dict-name-1") } returns NullNode.getInstance()

            every { raRuntimeService.getResolutionStore("naming-code") } returns "naming-123".asJsonPrimitive()
            every { raRuntimeService.getResolutionStore("naming-type") } returns "sample-type".asJsonPrimitive()
            every { raRuntimeService.getResolutionStore("naming-policy") } returns "sample-policy".asJsonPrimitive()
            every { raRuntimeService.getResolutionStore("cloud-region-id") } returns "cloud-123".asJsonPrimitive()

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

            val status = capabilityResourceResolutionProcessor.applyNB(resourceAssignments["ra-dict-name-1"]!!)
            assertTrue(status, "failed to execute capability source")
            // assertNotEquals(
            //     "assigned-data".asJsonPrimitive(), resourceAssignment1.property!!.value,
            //     "assigned value miss match"
            // )
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
            resourceDefinition("naming-policy", "naming-policy Resource Definition") {
                tags("naming-policy")
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
            resourceDefinition("ra-dict-name-1", "ra-dict-name-1 Resource Definition") {
                tags("ra-dict-name-1")
                updatedBy("brindasanth@onap.com")
                property("string", true)
                sources {
                    sourceCapability("naming-ms", "") {
                        definedProperties {
                            type("internal")
                            scriptClassReference(NamingResolutionCapability::class)
                            keyDependencies(
                                arrayListOf(
                                    "cloud-region-id",
                                    "naming-policy",
                                    "naming-type",
                                    "naming-code"
                                )
                            )
                        }
                    }
                }
            }
            resourceDefinition("ra-dict-name-2", "ra-dict-name-2 Resource Definition") {
                tags("ra-dict-name-2")
                updatedBy("brindasanth@onap.com")
                property("string", true)
                sources {
                    sourceCapability("naming-ms", "") {
                        definedProperties {
                            type("internal")
                            scriptClassReference(NamingResolutionCapability::class)
                            keyDependencies(
                                arrayListOf("ra-dict-name-1")
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
                                arrayListOf("ra-dict-name-1")
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
                name = "naming-policy", dictionaryName = "naming-policy",
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
                name = "ra-dict-name-1", dictionaryName = "ra-dict-name-1",
                dictionarySource = "naming-ms"
            ) {
                property("string", true, "")
                dependencies(
                    arrayListOf(
                        "naming-code",
                        "naming-type",
                        "naming-policy",
                        "cloud-region-id"
                    )
                )
            }
            resourceAssignment(
                name = "ra-dict-name-2", dictionaryName = "ra-dict-name-2",
                dictionarySource = "naming-ms"
            ) {
                property("string", true, "")
                dependencies(
                    arrayListOf(
                        "ra-dict-name-1"
                    )
                )
            }
            resourceAssignment(
                name = "ra-dict-name-3", dictionaryName = "ra-dict-name-3",
                dictionarySource = "naming-ms"
            ) {
                property("string", true, "")
                dependencies(
                    arrayListOf(
                        "ra-dict-name-1"
                    )
                )
            }
        }
    }
}
