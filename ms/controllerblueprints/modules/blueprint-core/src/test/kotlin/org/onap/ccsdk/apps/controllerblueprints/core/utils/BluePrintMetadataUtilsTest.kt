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

package org.onap.ccsdk.apps.controllerblueprints.core.utils


import org.junit.Test
import org.onap.ccsdk.apps.controllerblueprints.core.data.ToscaMetaData
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class BluePrintMetadataUtilsTest {

    @Test
    fun testToscaMetaData() {

        val basePath: String = "./../../../../components/model-catalog/blueprint-model/test-blueprint/baseconfiguration"

        val toscaMetaData: ToscaMetaData = BluePrintMetadataUtils.toscaMetaData(basePath)
        assertNotNull(toscaMetaData, "Missing Tosca Definition Object")
        assertNotNull(toscaMetaData.toscaMetaFileVersion, "Missing Tosca Metadata Version")
        assertNotNull(toscaMetaData.csarVersion, "Missing CSAR version")
        assertNotNull(toscaMetaData.createdBy, "Missing Created by")
        assertNotNull(toscaMetaData.entityDefinitions, "Missing Tosca Entity Definition")
        assertNotNull(toscaMetaData.templateTags, "Missing Template Tags")

    }

    @Test
    fun environmentDataTest() {
        val environmentPath = "./src/test/resources/environments"

        val properties = BluePrintMetadataUtils.bluePrintEnvProperties(environmentPath)

        assertNotNull(properties, "Could not read the properties")
        assertEquals(properties.getProperty("blueprintsprocessor.database.alt1.username"), "username1", "failed 1")
        assertEquals(properties.getProperty("blueprintsprocessor.database.alt1.password"), "password1", "failed 2")
        assertEquals(properties.getProperty("blueprintsprocessor.database.alt2.username"), "username2", "failed 3")
        assertEquals(properties.getProperty("blueprintsprocessor.database.alt2.password"), "password2", "failed 4")
        assertNull(properties.getProperty("blueprintsprocessor.database.alt3.password"), "failed 5")
    }
}