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

package org.onap.ccsdk.apps.blueprintsprocessor.functions.resource.resolution

import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.onap.ccsdk.apps.blueprintsprocessor.core.api.data.ExecutionServiceInput
import org.onap.ccsdk.apps.blueprintsprocessor.core.utils.PayloadUtils
import org.onap.ccsdk.apps.blueprintsprocessor.functions.resource.resolution.processor.*
import org.onap.ccsdk.apps.controllerblueprints.core.utils.BluePrintMetadataUtils
import org.onap.ccsdk.apps.controllerblueprints.core.utils.JacksonUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringRunner
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * ResourceResolutionServiceTest
 *
 * @author Brinda Santh DATE : 8/15/2018
 */
@RunWith(SpringRunner::class)
@ContextConfiguration(classes = [ResourceResolutionService::class,
    InputResourceAssignmentProcessor::class, DefaultResourceAssignmentProcessor::class,
    PrimaryDataResourceAssignmentProcessor::class, SimpleRestResourceAssignmentProcessor::class,
    CapabilityResourceAssignmentProcessor::class])
class ResourceResolutionServiceTest {

    private val log = LoggerFactory.getLogger(ResourceResolutionServiceTest::class.java)

    @Autowired
    lateinit var resourceResolutionService: ResourceResolutionService


    @Test
    fun testRegisteredSource() {
        val sources = resourceResolutionService.registeredResourceSources()
        assertNotNull(sources, "failed to get registered sources")
        assertTrue(sources.containsAll(arrayListOf("input", "default", "primary-db", "mdsal")), "failed to get registered sources")
    }

    @Test
    @Throws(Exception::class)
    fun testResolveResource() {

        Assert.assertNotNull("failed to create ResourceResolutionService", resourceResolutionService)

        val bluePrintRuntimeService = BluePrintMetadataUtils.getBluePrintRuntime("1234",
                "./../../../../components/model-catalog/blueprint-model/starter-blueprint/baseconfiguration")

        val executionServiceInput = JacksonUtils.readValueFromClassPathFile("payload/requests/sample-resourceresolution-request.json",
                ExecutionServiceInput::class.java)!!

        // Prepare Inputs
        PayloadUtils.prepareInputsFromWorkflowPayload(bluePrintRuntimeService, executionServiceInput.payload, "resource-assignment")

        resourceResolutionService.resolveResources(bluePrintRuntimeService, "resource-assignment", "baseconfig")

    }

}
