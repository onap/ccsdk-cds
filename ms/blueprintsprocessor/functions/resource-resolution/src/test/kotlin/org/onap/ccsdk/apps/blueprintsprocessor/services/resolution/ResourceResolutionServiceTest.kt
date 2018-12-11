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

package org.onap.ccsdk.apps.blueprintsprocessor.services.resolution

import com.fasterxml.jackson.databind.node.ObjectNode
import org.apache.commons.collections.CollectionUtils
import org.apache.commons.io.FileUtils
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.onap.ccsdk.apps.blueprintsprocessor.core.api.data.ResourceResolutionInput
import org.onap.ccsdk.apps.blueprintsprocessor.services.resolution.processor.DataBaseResourceAssignmentProcessor
import org.onap.ccsdk.apps.blueprintsprocessor.services.resolution.processor.DefaultResourceAssignmentProcessor
import org.onap.ccsdk.apps.blueprintsprocessor.services.resolution.processor.InputResourceAssignmentProcessor
import org.onap.ccsdk.apps.blueprintsprocessor.services.resolution.processor.SimpleRestResourceAssignmentProcessor
import org.onap.ccsdk.apps.controllerblueprints.core.utils.JacksonUtils
import org.onap.ccsdk.apps.controllerblueprints.resource.dict.ResourceAssignment
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringRunner
import java.io.File
import java.nio.charset.Charset
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
    DataBaseResourceAssignmentProcessor::class, SimpleRestResourceAssignmentProcessor::class])
class ResourceResolutionServiceTest {

    private val log = LoggerFactory.getLogger(ResourceResolutionServiceTest::class.java)

    @Autowired
    lateinit var resourceResolutionService: ResourceResolutionService


    @Test
    fun testRegisteredSource() {
        val sources = resourceResolutionService.registeredResourceSources()
        assertNotNull(sources, "failed to get registered sources")
        assertTrue(sources.containsAll(arrayListOf("input", "default", "db", "mdsal")), "failed to get registered sources")
    }

    @Test
    @Throws(Exception::class)
    fun testResolveResource() {

        Assert.assertNotNull("failed to create ResourceResolutionService", resourceResolutionService)

        val resourceResolutionInputContent = FileUtils.readFileToString(
                File("src/test/resources/payload/requests/sample-resourceresolution-request.json"), Charset.defaultCharset())

        val resourceResolutionInput = JacksonUtils.readValue(resourceResolutionInputContent, ResourceResolutionInput::class.java)
        Assert.assertNotNull("failed to populate resourceResolutionInput request ", resourceResolutionInput)

        val resourceAssignmentContent = FileUtils.readFileToString(
                File("src/test/resources/mapping/db/resource-assignments-simple.json"), Charset.defaultCharset())
        val batchResourceAssignment = JacksonUtils.getListFromJson(resourceAssignmentContent, ResourceAssignment::class.java)

        Assert.assertTrue("failed to create ResourceAssignment from file", CollectionUtils.isNotEmpty(batchResourceAssignment))
        resourceResolutionInput!!.resourceAssignments = batchResourceAssignment as MutableList<ResourceAssignment>

        val inputContent = JacksonUtils.jsonNodeFromFile("src/test/resources/payload/inputs/input.json") as ObjectNode
        Assert.assertNotNull("failed to populate input payload ", inputContent)
        resourceResolutionInput.payload = inputContent
        log.info("ResourceResolutionInput : {}", JacksonUtils.getJson(resourceResolutionInput, true))

        val resourceResolutionOutput = resourceResolutionService.resolveResource(resourceResolutionInput)
        Assert.assertNotNull("failed to populate output", resourceResolutionOutput)

    }

}
