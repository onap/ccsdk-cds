/*
 * Copyright © 2019 IBM, Bell Canada.
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

import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.onap.ccsdk.cds.blueprintsprocessor.core.BlueprintPropertiesService
import org.onap.ccsdk.cds.blueprintsprocessor.core.BlueprintPropertyConfiguration
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.ResourceAssignmentRuntimeService
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.mock.MockBlueprintRestLibPropertyService
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.mock.MockRestResourceResolutionProcessor
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.utils.ResourceAssignmentUtils
import org.onap.ccsdk.cds.blueprintsprocessor.rest.RestClientProperties
import org.onap.ccsdk.cds.controllerblueprints.core.data.PropertyDefinition
import org.onap.ccsdk.cds.controllerblueprints.core.utils.BlueprintMetadataUtils
import org.onap.ccsdk.cds.controllerblueprints.resource.dict.ResourceAssignment
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner
import kotlin.test.BeforeTest
import kotlin.test.assertNotNull

@RunWith(SpringRunner::class)
@ContextConfiguration(
    classes = [
        MockRestResourceResolutionProcessor::class, MockBlueprintRestLibPropertyService::class,
        BlueprintPropertyConfiguration::class, BlueprintPropertiesService::class, RestClientProperties::class
    ]
)
@TestPropertySource(locations = ["classpath:application-test.properties"])
class RestResourceResolutionProcessorTest {

    @Autowired
    lateinit var bluePrintRestLibPropertyService: MockBlueprintRestLibPropertyService

    private lateinit var restResourceResolutionProcessor: MockRestResourceResolutionProcessor

    @BeforeTest
    fun init() {
        restResourceResolutionProcessor = MockRestResourceResolutionProcessor(bluePrintRestLibPropertyService)
    }

    @Test
    fun `test rest resource resolution`() {
        runBlocking {
            val bluePrintContext = BlueprintMetadataUtils.getBlueprintContext(
                "./../../../../components/model-catalog/blueprint-model/test-blueprint/baseconfiguration"
            )

            val resourceAssignmentRuntimeService = ResourceAssignmentRuntimeService("1234", bluePrintContext)

            restResourceResolutionProcessor.raRuntimeService = resourceAssignmentRuntimeService
            restResourceResolutionProcessor.resourceDictionaries = ResourceAssignmentUtils
                .resourceDefinitions(bluePrintContext.rootPath)

            val scriptPropertyInstances: MutableMap<String, Any> = mutableMapOf()
            scriptPropertyInstances["mock-service1"] = MockCapabilityService()
            scriptPropertyInstances["mock-service2"] = MockCapabilityService()

            restResourceResolutionProcessor.scriptPropertyInstances = scriptPropertyInstances

            val resourceAssignment = ResourceAssignment().apply {
                name = "rr-name"
                dictionaryName = "vnf_name"
                dictionarySource = "sdnc"
                property = PropertyDefinition().apply {
                    type = "string"
                }
            }

            val processorName = restResourceResolutionProcessor.applyNB(resourceAssignment)
            assertNotNull(processorName, "couldn't get Rest resource assignment processor name")
            println(processorName)
        }
    }

    @Test
    fun `test rest aai get resource resolution`() {
        runBlocking {
            val bluePrintContext = BlueprintMetadataUtils.getBlueprintContext(
                "./../../../../components/model-catalog/blueprint-model/test-blueprint/baseconfiguration"
            )

            val resourceAssignmentRuntimeService = ResourceAssignmentRuntimeService("1234", bluePrintContext)

            restResourceResolutionProcessor.raRuntimeService = resourceAssignmentRuntimeService
            restResourceResolutionProcessor.resourceDictionaries = ResourceAssignmentUtils
                .resourceDefinitions(bluePrintContext.rootPath)

            val scriptPropertyInstances: MutableMap<String, Any> = mutableMapOf()
            scriptPropertyInstances["mock-service1"] = MockCapabilityService()
            scriptPropertyInstances["mock-service2"] = MockCapabilityService()

            restResourceResolutionProcessor.scriptPropertyInstances = scriptPropertyInstances

            val resourceAssignment = ResourceAssignment().apply {
                name = "rr-aai"
                dictionaryName = "aai-get-resource"
                dictionarySource = "aai-data"
                property = PropertyDefinition().apply {
                    type = "string"
                }
            }

            val processorName = restResourceResolutionProcessor.applyNB(resourceAssignment)
            assertNotNull(processorName, "couldn't get AAI Rest resource assignment processor name")
            println(processorName)
        }
    }

    @Test
    fun `test rest aai put resource resolution`() {
        runBlocking {
            val bluePrintContext = BlueprintMetadataUtils.getBlueprintContext(
                "./../../../../components/model-catalog/blueprint-model/test-blueprint/baseconfiguration"
            )

            val resourceAssignmentRuntimeService = ResourceAssignmentRuntimeService("1234", bluePrintContext)

            restResourceResolutionProcessor.raRuntimeService = resourceAssignmentRuntimeService
            restResourceResolutionProcessor.resourceDictionaries = ResourceAssignmentUtils
                .resourceDefinitions(bluePrintContext.rootPath)

            val scriptPropertyInstances: MutableMap<String, Any> = mutableMapOf()
            scriptPropertyInstances["mock-service1"] = MockCapabilityService()
            scriptPropertyInstances["mock-service2"] = MockCapabilityService()

            restResourceResolutionProcessor.scriptPropertyInstances = scriptPropertyInstances

            val resourceAssignment = ResourceAssignment().apply {
                name = "rr-aai"
                dictionaryName = "aai-put-resource"
                dictionarySource = "aai-data"
                property = PropertyDefinition().apply {
                    type = "string"
                }
            }

            val processorName = restResourceResolutionProcessor.applyNB(resourceAssignment)
            assertNotNull(processorName, "couldn't get AAI Rest resource assignment processor name")
            println(processorName)
        }
    }
}
