/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
 * Modifications Copyright © 2018 IBM.
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

package org.onap.ccsdk.cds.controllerblueprints.core.utils

import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.onap.ccsdk.cds.controllerblueprints.core.TestConstants
import java.io.File
import java.nio.file.Paths
import kotlin.test.assertTrue

class BlueprintFileUtilsTest {

    @Test
    fun testNewBlueprint() = runBlocking {
        val targetPath: String = Paths.get("target").toUri().toURL().path.plus("/bp-new-test")
        BlueprintFileUtils.createEmptyBlueprint(targetPath)
    }

    @Test
    fun testBlueprintCopy() = runBlocking {
        val sourcePath = TestConstants.PATH_TEST_BLUEPRINTS_BASECONFIG

        val targetPath: String = Paths.get("target").toUri().toURL().path.plus("/bp-copy-test")

        val targetDir = File(targetPath)
        targetDir.deleteOnExit()
        // Copy the BP file
        BlueprintFileUtils.copyBlueprint(sourcePath, targetDir.absolutePath)

        assertTrue(targetDir.exists(), "faield to copy blueprint to ${targetDir.absolutePath}")

        // Delete Type Files
        BlueprintFileUtils.deleteBlueprintTypes(targetDir.absolutePath)

        // Generate the Type Files
        val bluePrintContext = BlueprintMetadataUtils.getBlueprintContext(sourcePath)
        bluePrintContext.rootPath = targetDir.absolutePath

        BlueprintFileUtils.writeBlueprintTypes(bluePrintContext)
    }
}
