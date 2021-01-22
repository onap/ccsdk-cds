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
import org.onap.ccsdk.cds.controllerblueprints.core.data.ToscaMetaData
import org.onap.ccsdk.cds.controllerblueprints.core.normalizedPathName
import org.onap.ccsdk.cds.controllerblueprints.core.scripts.BlueprintCompileCache
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class BlueprintMetadataUtilsTest {

    @Test
    fun testToscaMetaData() {

        runBlocking {
            val basePath = TestConstants.PATH_TEST_BLUEPRINTS_BASECONFIG

            val toscaMetaData: ToscaMetaData = BlueprintMetadataUtils.toscaMetaData(basePath)
            assertNotNull(toscaMetaData, "Missing Tosca Definition Object")
            assertNotNull(toscaMetaData.toscaMetaFileVersion, "Missing Tosca Metadata Version")
            assertNotNull(toscaMetaData.csarVersion, "Missing CSAR version")
            assertNotNull(toscaMetaData.createdBy, "Missing Created by")
            assertNotNull(toscaMetaData.entityDefinitions, "Missing Tosca Entity Definition")
            assertNotNull(toscaMetaData.templateTags, "Missing Template Tags")
        }
    }

    @Test
    fun testKotlinBlueprintContext() {
        runBlocking {
            val path = normalizedPathName("src/test/resources/compile")
            val blueprintContext = BlueprintMetadataUtils.getBlueprintContext(path)
            assertNotNull(blueprintContext, "failed to get blueprint context")
            assertNotNull(blueprintContext.serviceTemplate, "failed to get blueprint context service template")
            assertNotNull(blueprintContext.serviceTemplate, "failed to get blueprint context service template")
            assertNotNull(blueprintContext.otherDefinitions, "failed to get blueprint contextother definitions")

            var cachePresent = BlueprintCompileCache.hasClassLoader(path)
            assertTrue(cachePresent, "failed to generate cache key ($path)")

            /** Cleaning Cache */
            BlueprintCompileCache.cleanClassLoader(path)
            cachePresent = BlueprintCompileCache.hasClassLoader(path)
            assertTrue(!cachePresent, "failed to remove cache key ($path)")
        }
    }

    @Test
    fun environmentDataTest() {
        val environmentPath = "./src/test/resources/environments"

        val properties = BlueprintMetadataUtils.bluePrintEnvProperties(environmentPath)

        assertNotNull(properties, "Could not read the properties")
        assertEquals(properties.getProperty("blueprintsprocessor.database.alt1.username"), "username1", "failed 1")
        assertEquals(properties.getProperty("blueprintsprocessor.database.alt1.password"), "password1", "failed 2")
        assertEquals(properties.getProperty("blueprintsprocessor.database.alt2.username"), "username2", "failed 3")
        assertEquals(properties.getProperty("blueprintsprocessor.database.alt2.password"), "password2", "failed 4")
        assertNull(properties.getProperty("blueprintsprocessor.database.alt3.password"), "failed 5")
    }
}
