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
import org.onap.ccsdk.cds.blueprintsprocessor.core.BluePrintPropertiesService
import org.onap.ccsdk.cds.blueprintsprocessor.core.BluePrintPropertyConfiguration
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.ResourceAssignmentRuntimeService
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.mock.MockBluePrintRestLibPropertyService
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.mock.MockRestResourceResolutionProcessor
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.utils.ResourceAssignmentUtils
import org.onap.ccsdk.cds.blueprintsprocessor.rest.RestClientProperties
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintConstants
import org.onap.ccsdk.cds.controllerblueprints.core.data.PropertyDefinition
import org.onap.ccsdk.cds.controllerblueprints.core.utils.BluePrintMetadataUtils
import org.onap.ccsdk.cds.controllerblueprints.resource.dict.ResourceAssignment
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(SpringRunner::class)
@ContextConfiguration(
    classes = [
        MockRestResourceResolutionProcessor::class, MockBluePrintRestLibPropertyService::class,
        BluePrintPropertyConfiguration::class, BluePrintPropertiesService::class, RestClientProperties::class
    ]
)
@TestPropertySource(locations = ["classpath:application-test.properties"])
class RestResourceResolutionProcessorTest {

    @Autowired
    lateinit var bluePrintRestLibPropertyService: MockBluePrintRestLibPropertyService

    private lateinit var restResourceResolutionProcessor: MockRestResourceResolutionProcessor

    @BeforeTest
    fun init() {
        restResourceResolutionProcessor = MockRestResourceResolutionProcessor(bluePrintRestLibPropertyService)
    }

    @AfterTest
    fun tearDown() {
        bluePrintRestLibPropertyService.tearDown()
    }

    @Test
    fun `test rest sdnc resource resolution`() {
        runBlocking {
            val bluePrintContext = BluePrintMetadataUtils.getBluePrintContext(
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
                    required = true
                }
            }

            val result = restResourceResolutionProcessor.applyNB(resourceAssignment)
            assertTrue(result, "get Rest resource assignment failed")
            assertEquals(
                resourceAssignment.status, BluePrintConstants.STATUS_SUCCESS,
                "get Rest resource assignment failed"
            )
            println(result)
        }
    }

    @Test
    fun `test rest aai get string resource resolution`() {
        runBlocking {
            val bluePrintContext = BluePrintMetadataUtils.getBluePrintContext(
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
                    required = true
                }
            }

            val result = restResourceResolutionProcessor.applyNB(resourceAssignment)
            assertTrue(result, "get AAI string Rest resource assignment failed")
            assertEquals(
                resourceAssignment.status, BluePrintConstants.STATUS_SUCCESS,
                "get AAI string Rest resource assignment failed"
            )
            println(resourceAssignment.status)
        }
    }

    @Test
    fun `test rest aai get json resource resolution`() {
        runBlocking {
            val bluePrintContext = BluePrintMetadataUtils.getBluePrintContext(
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
                    type = "json"
                    required = true
                }
            }

            val result = restResourceResolutionProcessor.applyNB(resourceAssignment)
            assertTrue(result, "get AAI json Rest resource assignment failed")
            assertEquals(
                resourceAssignment.status, BluePrintConstants.STATUS_SUCCESS,
                "get AAI json Rest resource assignment failed"
            )
            println(resourceAssignment.status)
        }
    }

    @Test
    fun `test rest aai put resource resolution`() {
        runBlocking {
            val bluePrintContext = BluePrintMetadataUtils.getBluePrintContext(
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

            val result = restResourceResolutionProcessor.applyNB(resourceAssignment)
            assertTrue(result, "put AAI Rest resource assignment failed")
            assertEquals(
                resourceAssignment.status, BluePrintConstants.STATUS_SUCCESS,
                "put AAI json Rest resource assignment failed"
            )
            println(resourceAssignment.status)
        }
    }
}
