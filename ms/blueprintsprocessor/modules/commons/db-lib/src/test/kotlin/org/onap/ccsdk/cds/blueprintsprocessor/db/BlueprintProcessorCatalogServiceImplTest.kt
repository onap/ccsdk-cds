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
import org.onap.ccsdk.cds.blueprintsprocessor.core.BluePrintCoreConfiguration
import org.onap.ccsdk.cds.blueprintsprocessor.db.mock.MockBlueprintProcessorCatalogServiceImpl
import org.onap.ccsdk.cds.blueprintsprocessor.db.primary.service.BlueprintCatalogServiceImpl
import org.onap.ccsdk.cds.blueprintsprocessor.db.primary.service.BlueprintProcessorCatalogServiceImpl
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintConstants
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintProcessorException
import org.onap.ccsdk.cds.controllerblueprints.core.compress
import org.onap.ccsdk.cds.controllerblueprints.core.deCompress
import org.onap.ccsdk.cds.controllerblueprints.core.deleteDir
import org.onap.ccsdk.cds.controllerblueprints.core.normalizedFile
import org.onap.ccsdk.cds.controllerblueprints.core.normalizedPath
import org.onap.ccsdk.cds.controllerblueprints.core.service.BluePrintRuntimeService
import org.onap.ccsdk.cds.controllerblueprints.core.utils.BluePrintMetadataUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.annotation.ComponentScan
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@RunWith(SpringRunner::class)
@EnableAutoConfiguration
@ComponentScan(basePackages = ["org.onap.ccsdk.cds.blueprintsprocessor"])
@ContextConfiguration(
    classes = [
        BlueprintProcessorCatalogServiceImpl::class, BluePrintCoreConfiguration::class,
        MockBlueprintProcessorCatalogServiceImpl::class
    ]
)
@TestPropertySource(locations = ["classpath:application-test.properties"])
class BlueprintProcessorCatalogServiceImplTest {

    @Autowired
    lateinit var blueprintsProcessorCatalogService: BlueprintCatalogServiceImpl

    @Autowired
    lateinit var blueprintCoreConfiguration: BluePrintCoreConfiguration

    private lateinit var bluePrintRuntimeService: BluePrintRuntimeService<*>

    private val blueprintId = "1234"

    @BeforeTest
    fun setup() {

        deleteDir("target", "blueprints")

        // Create sample CBA zip
        normalizedFile("./../../../../../components/model-catalog/blueprint-model/test-blueprint/baseconfiguration")
            .compress(normalizedFile("./target/blueprints/generated-cba.zip"))

        bluePrintRuntimeService = BluePrintMetadataUtils.bluePrintRuntime(
            blueprintId,
            "./../../../../../components/model-catalog/blueprint-model/test-blueprint/baseconfiguration"
        )
    }

    @AfterTest
    fun cleanDir() {
        deleteDir("target", "blueprints")
    }

    @Test
    fun `test catalog service`() {

        runBlocking {
            val file = normalizedFile("./target/blueprints/generated-cba.zip")
            assertTrue(file.exists(), "couldn't get file ${file.absolutePath}")

            blueprintsProcessorCatalogService.saveToDatabase("1234", file)
            blueprintsProcessorCatalogService.getFromDatabase("baseconfiguration", "1.0.0")

            blueprintsProcessorCatalogService.deleteFromDatabase("baseconfiguration", "1.0.0")
        }
    }

    @Test
    fun `test save function`() {
        runBlocking {
            val file = normalizedFile("./target/blueprints/generated-cba.zip")
            assertTrue(file.exists(), "couldnt get file ${file.absolutePath}")
            val ctx = bluePrintRuntimeService.bluePrintContext()
            val metadata = ctx.metadata!!
            metadata[BluePrintConstants.PROPERTY_BLUEPRINT_PROCESS_ID] = blueprintId

            blueprintsProcessorCatalogService.save(metadata, file, ctx.workflows()!!)
        }
    }

    @Test
    fun `test get function`() {
        runBlocking {
            val file = normalizedFile("./target/blueprints/generated-cba.zip")
            assertTrue(file.exists(), "couldnt get file ${file.absolutePath}")

            val ctx = bluePrintRuntimeService.bluePrintContext()
            val metadata = ctx.metadata!!
            metadata[BluePrintConstants.PROPERTY_BLUEPRINT_PROCESS_ID] = blueprintId

            blueprintsProcessorCatalogService.save(metadata, file, ctx.workflows()!!)
            blueprintsProcessorCatalogService.get("baseconfiguration", "1.0.0", true)
        }

        assertTrue(
            File(
                blueprintCoreConfiguration.bluePrintLoadConfiguration().blueprintArchivePath +
                    "/baseconfiguration"
            ).deleteRecursively(),
            "Couldn't get blueprint archive " +
                "${blueprintCoreConfiguration.bluePrintLoadConfiguration().blueprintArchivePath}/baseconfiguration " +
                "from data base."
        )
    }

    @Test
    fun `test get function creates idFile in deploy folder`() {
        runBlocking {
            val file = normalizedFile("./target/blueprints/generated-cba.zip")
            assertTrue(file.exists(), "couldn't get file ${file.absolutePath}")

            val ctx = bluePrintRuntimeService.bluePrintContext()
            val metadata = ctx.metadata!!
            metadata[BluePrintConstants.PROPERTY_BLUEPRINT_PROCESS_ID] = blueprintId

            blueprintsProcessorCatalogService.save(metadata, file, ctx.workflows()!!)
            val deployPath = blueprintsProcessorCatalogService.get("baseconfiguration", "1.0.0", true)

            assertNotNull(deployPath)

            val expectedIdFile = deployPath.resolve(blueprintId)
            assertTrue(
                Files.exists(expectedIdFile),
                "Id file does not exists in deploy folder"
            )
        }
    }

    @Test
    fun `test get function when blueprint doesn't exist in DB`() {
        runBlocking {
            val ex = assertFailsWith(exceptionClass = BluePrintProcessorException::class) {
                blueprintsProcessorCatalogService.get("baseconfiguration", "1.0.0", true)
            }
            assertEquals(
                ex.message?.contains("Blueprint does not exist in DB"),
                true,
                "Exception message should indicate that the blueprint does not exist in DB."
            )
        }
    }

    @Test
    fun `test get function to delete the cba local copy when not present in DB`() {
        runBlocking {

            val file = normalizedFile("./target/blueprints/generated-cba.zip")
            assertTrue(file.exists(), "couldn't get file ${file.absolutePath}")

            val tempCbaDeployFolderPath = createTempCBAInDeployFolder(file, "test_baseconfiguration", "1.0.0")

            assertTrue(Files.exists(tempCbaDeployFolderPath), "couldn't get path $tempCbaDeployFolderPath")

            assertFailsWith<BluePrintProcessorException> {
                blueprintsProcessorCatalogService.get("test_baseconfiguration", "1.0.0", true)
            }

            assertFalse(
                Files.exists(tempCbaDeployFolderPath),
                "Existing deploy folder should be deleted if CBA is not present in DB"
            )
        }
    }

    @Test
    fun `test get function to update the cba local copy with latest version`() {
        runBlocking {
            val file = normalizedFile("./target/blueprints/generated-cba.zip")
            assertTrue(file.exists(), "couldn't get file ${file.absolutePath}")

            val ctx = bluePrintRuntimeService.bluePrintContext()
            val metadata = ctx.metadata!!

            metadata[BluePrintConstants.PROPERTY_BLUEPRINT_PROCESS_ID] = blueprintId

            blueprintsProcessorCatalogService.save(metadata, file, ctx.workflows()!!)

            createTempCBAInDeployFolder(file, "baseconfiguration", "1.0.0")

            val deployPath = blueprintsProcessorCatalogService.get("baseconfiguration", "1.0.0", true)

            assertNotNull(deployPath)

            val expectedIdFile = deployPath.resolve(blueprintId)
            assertTrue(
                Files.exists(expectedIdFile),
                "Updated Id file does not exists in deploy folder"
            )
        }
    }

    @Test
    fun `test delete function`() {
        runBlocking {
            blueprintsProcessorCatalogService.delete("baseconfiguration", "1.0.0")
        }
    }

    private fun createTempCBAInDeployFolder(file: File, name: String, version: String): Path {
        val tempCbaDeployFolderPath = normalizedPath(blueprintCoreConfiguration.bluePrintLoadConfiguration().blueprintDeployPath, "/$name/$version")
        val tempBlueprintId = "4321"

        Files.createDirectories(tempCbaDeployFolderPath)

        file.deCompress(tempCbaDeployFolderPath.toString())

        val tempIdFilePath = tempCbaDeployFolderPath.resolve(tempBlueprintId)
        Files.createFile(tempIdFilePath)

        return tempCbaDeployFolderPath
    }
}
