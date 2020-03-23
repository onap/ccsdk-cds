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
import kotlinx.coroutines.runBlocking
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.ResourceAssignmentRuntimeService
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.processor.CapabilityResourceResolutionProcessor
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.processor.ResourceAssignmentProcessor
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.resourceAssignments
import org.onap.ccsdk.cds.blueprintsprocessor.services.execution.ComponentFunctionScriptingService
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintTypes
import org.onap.ccsdk.cds.controllerblueprints.core.asJsonPrimitive
import org.onap.ccsdk.cds.controllerblueprints.core.service.BluePrintContext
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * @author brindasanth
 */

class NamingResolutionCapabilityTest {

    // @Test
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
            every { raRuntimeService.getInputValue("ra-dict-name") } returns NullNode.getInstance()

            every { raRuntimeService.getResolutionStore("naming-code") } returns "naming-123".asJsonPrimitive()
            every { raRuntimeService.getResolutionStore("naming-type") } returns "sample-type".asJsonPrimitive()
            every { raRuntimeService.getResolutionStore("naming-policy") } returns "sample-policy".asJsonPrimitive()
            every { raRuntimeService.getResolutionStore("cloud-region-id") } returns "cloud-123".asJsonPrimitive()

            val capabilityResourceResolutionProcessor =
                CapabilityResourceResolutionProcessor(componentFunctionScriptingService)
            capabilityResourceResolutionProcessor.raRuntimeService = raRuntimeService

            val resourceAssignments = BluePrintTypes.resourceAssignments {
                resourceAssignment(
                    name = "ra-dict-name", dictionaryName = "ra-dict-name",
                    dictionarySource = "capability"
                ) {
                    property("string", true, "")
                    sourceCapability {
                        definedProperties {
                            type("internal")
                            scriptClassReference(NamingResolutionCapability::class)
                            keyDependencies(
                                arrayListOf(
                                    "naming-code",
                                    "naming-type",
                                    "naming-policy",
                                    "cloud-region-id"
                                )
                            )
                        }
                    }
                }
                resourceAssignment(
                    name = "ra-dict-name-2", dictionaryName = "ra-dict-name-2",
                    dictionarySource = "capability"
                ) {
                    property("string", true, "")
                    sourceCapability {
                        definedProperties {
                            type("internal")
                            scriptClassReference(NamingResolutionCapability::class)
                            keyDependencies(
                                arrayListOf(
                                    "ra-dict-name"
                                )
                            )
                        }
                    }
                }
                resourceAssignment(
                    name = "ra-dict-name-3", dictionaryName = "ra-dict-name-3",
                    dictionarySource = "capability"
                ) {
                    property("string", true, "")
                    sourceCapability {
                        definedProperties {
                            type("internal")
                            scriptClassReference(NamingResolutionCapability::class)
                            keyDependencies(
                                arrayListOf(
                                    "ra-dict-name"
                                )
                            )
                        }
                    }
                }
            }

            capabilityResourceResolutionProcessor.resourceAssignments = resourceAssignments.values.toMutableList()

            //TODO("Mock Rest calls and enable")
            // val status =
            //     capabilityResourceResolutionProcessor.applyNB(resourceAssignments["ra-dict-name"]!!)
            // assertTrue(status, "failed to execute capability source")
            // assertEquals(
            //     "assigned-data".asJsonPrimitive(), resourceAssignment1.property!!.value,
            //     "assigned value miss match"
            // )
        }
    }
}
