/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
 * ================================================================================
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
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */
package org.onap.ccsdk.cds.blueprintsprocessor.uat

import kotlinx.coroutines.runBlocking
import org.junit.ClassRule
import org.junit.Rule
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.onap.ccsdk.cds.blueprintsprocessor.uat.utils.ExtendedTemporaryFolder
import org.onap.ccsdk.cds.blueprintsprocessor.uat.utils.UatExecutor
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintConstants.UAT_SPECIFICATION_FILE
import org.onap.ccsdk.cds.controllerblueprints.core.utils.BluePrintArchiveUtils.Companion.compressToBytes
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.junit4.rules.SpringClassRule
import org.springframework.test.context.junit4.rules.SpringMethodRule
import java.io.File
import java.nio.file.FileSystem
import java.nio.file.FileSystems
import kotlin.test.BeforeTest
import kotlin.test.Test

// Only one runner can be configured with jUnit 4. We had to replace the SpringRunner by equivalent jUnit rules.
// See more on https://docs.spring.io/autorepo/docs/spring-framework/current/spring-framework-reference/testing.html#testcontext-junit4-rules
@RunWith(Parameterized::class)
class BlueprintsAcceptanceTest(
    @Suppress("unused") private val blueprintName: String, // readable test description
    private val rootFs: FileSystem
) : BaseUatTest() {

    companion object {

        @ClassRule
        @JvmField
        val springClassRule = SpringClassRule()

        /**
         * Generates the parameters to create a test instance for every blueprint found under UAT_BLUEPRINTS_BASE_DIR
         * that contains the proper UAT definition file.
         */
        @Parameterized.Parameters(name = "{index} {0}")
        @JvmStatic
        fun scanUatEmpoweredBlueprints(): List<Array<Any>> {
            return (File(UAT_BLUEPRINTS_BASE_DIR)
                .listFiles { file -> file.isDirectory && File(file, UAT_SPECIFICATION_FILE).isFile }
                ?: throw RuntimeException("Failed to scan $UAT_BLUEPRINTS_BASE_DIR"))
                .map { file ->
                    arrayOf(
                        file.nameWithoutExtension,
                        FileSystems.getFileSystem(file.canonicalFile.toURI()) ?: FileSystems.newFileSystem(file.canonicalFile.toPath(), null)
                    )
                }
        }
    }

    @Rule
    @JvmField
    val springMethodRule = SpringMethodRule()

    @Autowired
    // Bean is created programmatically by {@link WorkingFoldersInitializer#initialize(String)}
    @Suppress("SpringJavaInjectionPointsAutowiringInspection")
    lateinit var tempFolder: ExtendedTemporaryFolder

    @Autowired
    lateinit var uatExecutor: UatExecutor

    @BeforeTest
    fun cleanupTemporaryFolder() {
        tempFolder.deleteAllFiles()
    }

    @Test
    fun runUat() {
        runBlocking {
            val uatSpec = rootFs.getPath(UAT_SPECIFICATION_FILE).toFile().readText()
            val cbaBytes = compressToBytes(rootFs.getPath("/"))
            uatExecutor.execute(uatSpec, cbaBytes)
        }
    }
}
