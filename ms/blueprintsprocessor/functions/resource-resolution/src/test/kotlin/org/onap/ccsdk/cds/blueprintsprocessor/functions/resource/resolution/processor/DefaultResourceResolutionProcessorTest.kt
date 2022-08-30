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

import com.fasterxml.jackson.databind.node.TextNode
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.ResourceAssignmentRuntimeService
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintConstants
import org.onap.ccsdk.cds.controllerblueprints.core.data.PropertyDefinition
import org.onap.ccsdk.cds.controllerblueprints.core.utils.BluePrintMetadataUtils
import org.onap.ccsdk.cds.controllerblueprints.resource.dict.ResourceAssignment
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(SpringRunner::class)
@ContextConfiguration(classes = [DefaultResourceResolutionProcessor::class])
@TestPropertySource(locations = ["classpath:application-test.properties"])
class DefaultResourceResolutionProcessorTest {

    @Autowired
    lateinit var defaultResourceResolutionProcessor: DefaultResourceResolutionProcessor

    @Test
    fun `test default resource resolution`() {
        runBlocking {
            val bluePrintContext = BluePrintMetadataUtils.getBluePrintContext(
                "./../../../../components/model-catalog/blueprint-model/test-blueprint/baseconfiguration"
            )

            val resourceAssignmentRuntimeService = ResourceAssignmentRuntimeService("1234", bluePrintContext)

            defaultResourceResolutionProcessor.raRuntimeService = resourceAssignmentRuntimeService
            defaultResourceResolutionProcessor.resourceDictionaries = hashMapOf()

            val resourceAssignment = ResourceAssignment().apply {
                name = "rr-name"
                dictionaryName = "rr-dict-name"
                dictionarySource = "default"
                property = PropertyDefinition().apply {
                    type = "string"
                    defaultValue = TextNode("test")
                    required = true
                }
            }

            val result = defaultResourceResolutionProcessor.applyNB(resourceAssignment)
            assertTrue(result, "An error occurred while trying to test the DefaultResourceResolutionProcessor")
            assertEquals(
                resourceAssignment.status, BluePrintConstants.STATUS_SUCCESS,
                "An error occurred while trying to test the DefaultResourceResolutionProcessor"
            )
        }
    }
}
