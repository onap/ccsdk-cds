/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
 *
 * Modifications Copyright © 2019 IBM, Bell Canada.
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

package org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.processor

import com.fasterxml.jackson.databind.node.NullNode
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.ResourceAssignmentRuntimeService
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.resourceAssignment
import org.onap.ccsdk.cds.blueprintsprocessor.services.execution.ComponentFunctionScriptingService
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintConstants
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintTypes
import org.onap.ccsdk.cds.controllerblueprints.core.asJsonPrimitive
import org.onap.ccsdk.cds.controllerblueprints.core.data.PropertyDefinition
import org.onap.ccsdk.cds.controllerblueprints.core.logger
import org.onap.ccsdk.cds.controllerblueprints.core.service.BlueprintContext
import org.onap.ccsdk.cds.controllerblueprints.core.utils.BlueprintMetadataUtils
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import org.onap.ccsdk.cds.controllerblueprints.resource.dict.ResourceAssignment
import org.onap.ccsdk.cds.controllerblueprints.resource.dict.ResourceDefinition
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class CapabilityResourceResolutionProcessorTest {

    @Test
    fun `test kotlin capability`() {
        runBlocking {
            val componentFunctionScriptingService = mockk<ComponentFunctionScriptingService>()
            coEvery {
                componentFunctionScriptingService
                    .scriptInstance<ResourceAssignmentProcessor>(any(), any(), any())
            } returns MockCapabilityScriptRA()

            val raRuntimeService = mockk<ResourceAssignmentRuntimeService>()
            every { raRuntimeService.bluePrintContext() } returns mockk<BlueprintContext>()
            every { raRuntimeService.getInputValue("test-property") } returns NullNode.getInstance()

            val capabilityResourceResolutionProcessor =
                CapabilityResourceResolutionProcessor(componentFunctionScriptingService)
            capabilityResourceResolutionProcessor.raRuntimeService = raRuntimeService

            val resourceAssignment = BlueprintTypes.resourceAssignment(
                name = "test-property", dictionaryName = "ra-dict-name",
                dictionarySource = "capability"
            ) {
                property("string", true, "")
                sourceCapability {
                    definedProperties {
                        type("internal")
                        scriptClassReference(MockCapabilityScriptRA::class.qualifiedName!!)
                        keyDependencies(arrayListOf("dep-property"))
                    }
                }
            }
            val status = capabilityResourceResolutionProcessor.applyNB(resourceAssignment)
            assertTrue(status, "failed to execute capability source")
            assertEquals(
                "assigned-data".asJsonPrimitive(), resourceAssignment.property!!.value,
                "assigned value miss match"
            )
        }
    }

    @Test
    fun `test jython capability`() {
        runBlocking {

            val bluePrintContext = BlueprintMetadataUtils.getBlueprintContext(
                "./../../../../components/model-catalog/blueprint-model/test-blueprint/capability_python"
            )

            val componentFunctionScriptingService = mockk<ComponentFunctionScriptingService>()
            coEvery {
                componentFunctionScriptingService
                    .scriptInstance<ResourceAssignmentProcessor>(any(), BlueprintConstants.SCRIPT_JYTHON, any())
            } returns MockCapabilityScriptRA()

            val resourceAssignmentRuntimeService = ResourceAssignmentRuntimeService("1234", bluePrintContext)

            val capabilityResourceResolutionProcessor =
                CapabilityResourceResolutionProcessor(componentFunctionScriptingService)

            capabilityResourceResolutionProcessor.raRuntimeService = resourceAssignmentRuntimeService

            val resourceDefinition = JacksonUtils
                .readValueFromClassPathFile(
                    "mapping/capability/jython-resource-definitions.json",
                    ResourceDefinition::class.java
                )!!
            val resourceDefinitions: MutableMap<String, ResourceDefinition> = mutableMapOf()
            resourceDefinitions[resourceDefinition.name] = resourceDefinition
            capabilityResourceResolutionProcessor.resourceDictionaries = resourceDefinitions

            val resourceAssignment = ResourceAssignment().apply {
                name = "service-instance-id"
                dictionaryName = "service-instance-id"
                dictionarySource = "capability"
                property = PropertyDefinition().apply {
                    type = "string"
                }
            }

            val processorName = capabilityResourceResolutionProcessor.processNB(resourceAssignment)
            assertNotNull(processorName, "couldn't get Jython script resource assignment processor name")
            println(processorName)
        }
    }
}

open class MockCapabilityService

open class MockCapabilityScriptRA : ResourceAssignmentProcessor() {

    val log = logger(MockCapabilityScriptRA::class)

    override fun getName(): String {
        return "MockCapabilityScriptRA"
    }

    override suspend fun processNB(executionRequest: ResourceAssignment) {
        log.info("executing RA mock capability : ${executionRequest.name}")
        executionRequest.property!!.value = "assigned-data".asJsonPrimitive()
    }

    override fun process(executionRequest: ResourceAssignment) {
        log.info("executing RA mock capability : ${executionRequest.name}")
        executionRequest.property!!.value = "assigned-data".asJsonPrimitive()
    }

    override suspend fun recoverNB(runtimeException: RuntimeException, executionRequest: ResourceAssignment) {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }
}
