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

package org.onap.ccsdk.cds.controllerblueprints.core

import org.junit.Test
import kotlin.test.assertTrue

class FileExtensionFunctionTest {

    val blueprintBasePath = TestConstants.PATH_TEST_BLUEPRINTS_BASECONFIG

    @Test
    fun testCompress() {
        val blueprintFile = normalizedFile(blueprintBasePath)
        val targetZip = normalizedFile("target", "compress.zip")
        targetZip.deleteRecursively()
        blueprintFile.compress(targetZip)
        assertTrue(blueprintFile.exists(), "failed to create compressed file(${targetZip.absolutePath} ")
    }
}
