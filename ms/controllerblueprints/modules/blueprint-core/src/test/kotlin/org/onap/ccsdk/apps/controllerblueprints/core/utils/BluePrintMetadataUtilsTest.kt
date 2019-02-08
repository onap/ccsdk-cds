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
import kotlin.test.assertNotNull

class BluePrintMetadataUtilsTest {
    
    @Test
    fun testToscaMetaData(){

        val basePath : String = "./../../../../components/model-catalog/blueprint-model/test-blueprint/baseconfiguration"

        val toscaMetaData : ToscaMetaData =  BluePrintMetadataUtils.toscaMetaData(basePath)
        assertNotNull(toscaMetaData, "Missing Tosca Definition Object")
        assertNotNull(toscaMetaData.toscaMetaFileVersion, "Missing Tosca Metadata Version")
        assertNotNull(toscaMetaData.csarVersion, "Missing CSAR version")
        assertNotNull(toscaMetaData.createdBy, "Missing Created by")
        assertNotNull(toscaMetaData.entityDefinitions, "Missing Tosca Entity Definition")
        assertNotNull(toscaMetaData.templateTags, "Missing Template Tags")

    }
}