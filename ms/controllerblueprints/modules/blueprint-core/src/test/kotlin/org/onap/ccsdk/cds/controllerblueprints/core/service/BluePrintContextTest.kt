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


import com.att.eelf.configuration.EELFLogger
import com.att.eelf.configuration.EELFManager
import org.junit.Test
import org.onap.ccsdk.cds.controllerblueprints.core.utils.BluePrintMetadataUtils
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import kotlin.test.assertNotNull

/**
 *
 *
 * @author Brinda Santh
 */
class BluePrintContextTest {

    private val log: EELFLogger = EELFManager.getInstance().getLogger(this::class.toString())

    val blueprintBasePath: String = ("./../../../../components/model-catalog/blueprint-model/test-blueprint/baseconfiguration")

    @Test
    fun testBluePrintContextCreation() {
        val bluePrintContext = BluePrintMetadataUtils.getBluePrintContext(blueprintBasePath)
        assertNotNull(bluePrintContext, "Failed to populate Blueprint context")
    }

    @Test
    fun testChainedProperty() {
        val bluePrintContext = BluePrintMetadataUtils.getBluePrintContext(blueprintBasePath)
        val nodeType = bluePrintContext.nodeTypeChained("component-resource-resolution")
        assertNotNull(nodeType, "Failed to get chained node type")
        log.trace("Properties {}", JacksonUtils.getJson(nodeType, true))
    }


}
