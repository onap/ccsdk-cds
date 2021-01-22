/*
 *  Copyright © 2019 IBM.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.onap.ccsdk.cds.blueprintsprocessor.designer.api.utils

import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.onap.ccsdk.cds.blueprintsprocessor.designer.api.mock.MockFilePart
import org.onap.ccsdk.cds.controllerblueprints.core.compress
import org.onap.ccsdk.cds.controllerblueprints.core.deleteDir
import org.onap.ccsdk.cds.controllerblueprints.core.normalizedFile
import org.onap.ccsdk.cds.controllerblueprints.core.normalizedPathName
import org.onap.ccsdk.cds.controllerblueprints.core.reCreateDirs
import java.util.UUID
import kotlin.test.assertTrue

class BlueprintEnhancerUtilsTest {

    private val blueprintDir = "./../../../../../components/model-catalog/blueprint-model/test-blueprint/baseconfiguration"
    private val blueprintArchivePath: String = "./target/blueprints/archive"
    private val blueprintEnrichmentPath: String = "./target/blueprints/enrichment"
    private var zipBlueprintFileName = normalizedPathName(blueprintArchivePath, "test.zip")

    @Before
    fun setUp() {
        val archiveDir = normalizedFile(blueprintArchivePath).reCreateDirs()
        assertTrue(archiveDir.exists(), "failed to create archiveDir(${archiveDir.absolutePath}")
        val enhancerDir = normalizedFile(blueprintEnrichmentPath).reCreateDirs()
        assertTrue(enhancerDir.exists(), "failed to create enhancerDir(${enhancerDir.absolutePath}")
        val blueprintFile = normalizedFile(blueprintDir)
        val testZipFile = blueprintFile.compress(zipBlueprintFileName)
        assertTrue(testZipFile.exists(), "Failed to create blueprint test zip(${testZipFile.absolutePath}")
    }

    @After
    fun tearDown() {
        deleteDir(blueprintArchivePath)
        deleteDir(blueprintEnrichmentPath)
    }

    @Test
    fun testFilePartCompressionNDeCompression() {
        val filePart = MockFilePart(zipBlueprintFileName)

        runBlocking {
            val enhanceId = UUID.randomUUID().toString()
            val blueprintArchiveLocation = normalizedPathName(blueprintArchivePath, enhanceId)
            val blueprintEnrichmentLocation = normalizedPathName(blueprintEnrichmentPath, enhanceId)
            BlueprintEnhancerUtils.copyFilePartToEnhanceDir(filePart, blueprintArchiveLocation, blueprintEnrichmentLocation)
            BlueprintEnhancerUtils.compressEnhanceDirAndReturnFilePart(blueprintEnrichmentLocation, blueprintArchiveLocation)
        }
    }
}
