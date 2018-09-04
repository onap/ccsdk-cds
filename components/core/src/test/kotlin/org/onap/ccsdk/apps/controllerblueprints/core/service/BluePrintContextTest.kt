/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
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

package org.onap.ccsdk.apps.controllerblueprints.core.service


import org.apache.commons.io.FileUtils
import org.junit.Before
import org.junit.Test
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintConstants
import org.onap.ccsdk.apps.controllerblueprints.core.factory.BluePrintParserFactory
import org.onap.ccsdk.apps.controllerblueprints.core.utils.JacksonUtils
import com.att.eelf.configuration.EELFLogger
import com.att.eelf.configuration.EELFManager
import java.io.File
import java.nio.charset.Charset
import kotlin.test.assertNotNull
/**
 *
 *
 * @author Brinda Santh
 */
class BluePrintContextTest {

    private val log: EELFLogger = EELFManager.getInstance().getLogger(this::class.toString())

    lateinit var bluePrintContext: BluePrintContext

    @Before
    fun setUp() {

        val basepath = "load/blueprints"

        bluePrintContext = BluePrintParserFactory.instance(BluePrintConstants.TYPE_DEFAULT)!!
                .readBlueprintFile("baseconfiguration/Definitions/activation-blueprint.json", basepath)
        assertNotNull(bluePrintContext, "Failed to populate Blueprint context")
    }

    @Test
    fun testBluePrintContextFromContent() {
        val fileName = "load/blueprints/baseconfiguration/Definitions/activation-blueprint.json"
        val content : String = FileUtils.readFileToString(File(fileName), Charset.defaultCharset())
        val bpContext  = BluePrintParserFactory.instance(BluePrintConstants.TYPE_DEFAULT)!!
                .readBlueprint(content)
        assertNotNull(bpContext, "Failed to get blueprint content")
        assertNotNull(bpContext.serviceTemplate, "Failed to get blueprint content's service template")
    }

    @Test
    fun testChainedProperty() {
        val nodeType = bluePrintContext.nodeTypeChained("component-resource-assignment")
        assertNotNull(nodeType, "Failed to get chained node type")
        log.trace("Properties {}", JacksonUtils.getJson(nodeType, true))
    }


}
