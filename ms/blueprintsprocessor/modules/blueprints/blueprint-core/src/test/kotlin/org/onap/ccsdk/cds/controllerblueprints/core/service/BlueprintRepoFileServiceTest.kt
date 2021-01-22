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

package org.onap.ccsdk.cds.controllerblueprints.core.service

import org.junit.Test
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintException
import org.onap.ccsdk.cds.controllerblueprints.core.TestConstants
import kotlin.test.assertNotNull

/**
 * BlueprintRepoFileServiceTest
 * @author Brinda Santh
 *
 */
class BlueprintRepoFileServiceTest {

    private val basePath = TestConstants.PATH_TEST_DEFINITION_TYPE_STARTER
    private val bluePrintRepoFileService = BlueprintRepoFileService(basePath)

    @Test
    fun testGetDataType() {
        val dataType = bluePrintRepoFileService.getDataType("dt-v4-aggregate")
        assertNotNull(dataType, "Failed to get DataType from repo")
    }

    @Test
    fun testGetNodeType() {
        val nodeType = bluePrintRepoFileService.getNodeType("component-resource-resolution")
        assertNotNull(nodeType, "Failed to get NodeType from repo")
    }

    @Test
    fun testGetArtifactType() {
        val nodeType = bluePrintRepoFileService.getArtifactType("artifact-template-velocity")
        assertNotNull(nodeType, "Failed to get ArtifactType from repo")
    }

    @Test(expected = BlueprintException::class)
    fun testModelNotFound() {
        val dataType = bluePrintRepoFileService.getDataType("dt-not-found")
        assertNotNull(dataType, "Failed to get DataType from repo")
    }
}
