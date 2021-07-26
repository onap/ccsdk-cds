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
import org.onap.ccsdk.cds.blueprintsprocessor.db.PrimaryDBLibGenericService
import org.onap.ccsdk.cds.blueprintsprocessor.db.primary.BluePrintDBLibPropertyService
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.ResourceAssignmentRuntimeService
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.TestDatabaseConfiguration
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.mock.MockBlueprintProcessorCatalogServiceImpl
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.mock.MockDBLibGenericService
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.utils.ResourceAssignmentUtils
import org.onap.ccsdk.cds.controllerblueprints.core.data.PropertyDefinition
import org.onap.ccsdk.cds.controllerblueprints.core.utils.BluePrintMetadataUtils
import org.onap.ccsdk.cds.controllerblueprints.resource.dict.ResourceAssignment
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner
import kotlin.test.assertNotNull

@RunWith(SpringRunner::class)
@ContextConfiguration(
    classes = [
        TestDatabaseConfiguration::class,
        PrimaryDBLibGenericService::class, BluePrintDBLibPropertyService::class,
        DatabaseResourceAssignmentProcessor::class, MockDBLibGenericService::class,
        MockBlueprintProcessorCatalogServiceImpl::class
    ]
)
@TestPropertySource(locations = ["classpath:application-test.properties"])
class DatabaseResourceResolutionProcessorTest {

    @Autowired
    lateinit var databaseResourceAssignmentProcessor: DatabaseResourceAssignmentProcessor

    @Test
    fun `test database resource resolution processor db`() {
        runBlocking {
            val bluePrintContext = BluePrintMetadataUtils.getBluePrintContext(
                "./../../../../components/model-catalog/blueprint-model/test-blueprint/baseconfiguration"
            )

            val resourceAssignmentRuntimeService = ResourceAssignmentRuntimeService("1234", bluePrintContext)

            databaseResourceAssignmentProcessor.raRuntimeService = resourceAssignmentRuntimeService
            databaseResourceAssignmentProcessor.resourceDictionaries = ResourceAssignmentUtils
                .resourceDefinitions(bluePrintContext.rootPath)

            val resourceAssignment = ResourceAssignment().apply {
                name = "service-instance-id"
                dictionaryName = "service-instance-id"
                dictionarySource = "processor-db"
                property = PropertyDefinition().apply {
                    type = "string"
                }
            }

            val processorName = databaseResourceAssignmentProcessor.applyNB(resourceAssignment)
            assertNotNull(processorName, "couldn't get Database resource assignment processor name")
        }
    }
}
