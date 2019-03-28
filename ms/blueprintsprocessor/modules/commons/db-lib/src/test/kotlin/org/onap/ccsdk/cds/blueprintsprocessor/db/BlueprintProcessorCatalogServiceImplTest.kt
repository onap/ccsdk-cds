/*
 * Copyright (C) 2019 Bell Canada.
 *
 * Copyright (C) 2019 IBM, Bell Canada.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onap.ccsdk.cds.blueprintsprocessor.db

import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.onap.ccsdk.cds.controllerblueprints.core.deleteDir
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BluePrintCatalogService
import org.onap.ccsdk.cds.controllerblueprints.core.normalizedFile
import org.onap.ccsdk.cds.blueprintsprocessor.core.BluePrintCoreConfiguration
import org.onap.ccsdk.cds.blueprintsprocessor.db.mock.MockBlueprintProcessorCatalogServiceImpl
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintConstants
import org.onap.ccsdk.cds.controllerblueprints.core.service.BluePrintRuntimeService
import org.onap.ccsdk.cds.controllerblueprints.core.utils.BluePrintMetadataUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.annotation.ComponentScan
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner
import java.io.File
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.assertTrue

@RunWith(SpringRunner::class)
@EnableAutoConfiguration
@ComponentScan(basePackages = ["org.onap.ccsdk.cds.blueprintsprocessor", "org.onap.ccsdk.cds.controllerblueprints"])
@ContextConfiguration(classes = [BlueprintProcessorCatalogServiceImpl::class, BluePrintCoreConfiguration::class,
    MockBlueprintProcessorCatalogServiceImpl::class])
@TestPropertySource(locations = ["classpath:application-test.properties"])
class BlueprintProcessorCatalogServiceImplTest {

    @Autowired
    lateinit var blueprintCatalog: BluePrintCatalogService

    @Autowired
    lateinit var blueprintProcessorCatalogServiceImpl: BlueprintProcessorCatalogServiceImpl

    @Autowired
    lateinit var blueprintCoreConfiguration: BluePrintCoreConfiguration

    private lateinit var bluePrintRuntimeService: BluePrintRuntimeService<*>

    private val blueprintId = "1234"

    @BeforeTest
    fun setup() {
        deleteDir("target", "blueprints")
        bluePrintRuntimeService = BluePrintMetadataUtils.getBluePrintRuntime(blueprintId,
                    "./../../../../../components/model-catalog/blueprint-model/test-blueprint/baseconfiguration")
    }

    @AfterTest
    fun cleanDir() {
        deleteDir("target", "blueprints")
    }

    @Test
    fun `test catalog service`() {
        //TODO: I thing this test function should be remve and replace by the other one.
        runBlocking {
            //FIXME("Create ZIP from test blueprints")

            val file = normalizedFile("./src/test/resources/test-cba.zip")
            assertTrue(file.exists(), "couldn't get file ${file.absolutePath}")

            blueprintCatalog.saveToDatabase("1234", file)
            blueprintCatalog.getFromDatabase("baseconfiguration", "1.0.0")

            blueprintCatalog.deleteFromDatabase("baseconfiguration", "1.0.0")
        }
    }

    @Test
    fun `test save function`() {
        runBlocking {
            val file = normalizedFile("./src/test/resources/test-cba.zip")
            assertTrue(file.exists(), "couldnt get file ${file.absolutePath}")
            val metadata = bluePrintRuntimeService.bluePrintContext().metadata!!
            metadata[BluePrintConstants.PROPERTY_BLUEPRINT_PROCESS_ID] = blueprintId

            blueprintProcessorCatalogServiceImpl.save(metadata, file)
        }
    }

    @Test
    fun `test get function`() {
        runBlocking {
            val file = normalizedFile("./src/test/resources/test-cba.zip")
            assertTrue(file.exists(), "couldnt get file ${file.absolutePath}")
            val metadata = bluePrintRuntimeService.bluePrintContext().metadata!!
            metadata[BluePrintConstants.PROPERTY_BLUEPRINT_PROCESS_ID] = blueprintId

            blueprintProcessorCatalogServiceImpl.save(metadata, file)
            blueprintProcessorCatalogServiceImpl.get("baseconfiguration", "1.0.0", true)
        }

        assertTrue(File(blueprintCoreConfiguration.bluePrintPathConfiguration().blueprintArchivePath +
                "/baseconfiguration").deleteRecursively(),"Couldn't get blueprint archive " +
                "${blueprintCoreConfiguration.bluePrintPathConfiguration().blueprintArchivePath}/baseconfiguration " +
                "from data base.")
    }

    @Test
    fun `test delete function`() {
        runBlocking {
            blueprintProcessorCatalogServiceImpl.delete("baseconfiguration", "1.0.0")
        }
    }
}