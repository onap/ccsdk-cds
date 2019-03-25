/*
 * Copyright (C) 2019 Bell Canada.
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

import org.junit.Test
import org.junit.runner.RunWith
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BluePrintCatalogService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.annotation.ComponentScan
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner
import java.io.File
import java.nio.file.Paths
import kotlin.test.assertTrue

@RunWith(SpringRunner::class)
@EnableAutoConfiguration
@ComponentScan(basePackages = ["org.onap.ccsdk.cds.blueprintsprocessor", "org.onap.ccsdk.cds.controllerblueprints"])
@TestPropertySource(locations = ["classpath:application-test.properties"])
class BlueprintProcessorCatalogServiceImplTest {

    @Autowired
    lateinit var blueprintCatalog: BluePrintCatalogService

    @Test
    fun `test catalog service`() {
        val file = Paths.get("./src/test/resources/test-cba.zip").toFile()
        assertTrue(file.exists(), "couldnt get file ${file.absolutePath}")

        blueprintCatalog.saveToDatabase(file)

        blueprintCatalog.getFromDatabase("baseconfiguration", "1.0.0")

        blueprintCatalog.deleteFromDatabase("baseconfiguration", "1.0.0")

        File("./src/test/resources/baseconfiguration").deleteRecursively()
    }
}