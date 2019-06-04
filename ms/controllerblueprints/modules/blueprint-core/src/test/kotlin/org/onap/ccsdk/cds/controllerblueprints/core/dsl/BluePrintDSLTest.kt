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

package org.onap.ccsdk.cds.controllerblueprints.core.dsl

import org.junit.Test
import kotlin.test.assertNotNull

class BluePrintDSLTest {
    @Test
    fun testServiceTemplate() {
        val serviceTemplate = serviceTemplate("sample-bp", "1.0.0",
                "brindasanth@onap.com", "sample") {
            metadata("release", "1806")
            topologyTemplate {
                nodeTemplateOperation(nodeTemplateName = "activate", type = "sample-node-type", interfaceName = "RestconfExecutor",
                        operationName = "process", description = "sample activation") {
                    inputs {
                        property("json-content", """{ "name" : "cds"}""")
                        property("array-content", """["controller", "blueprints"]""")
                        property("int-value", 234)
                        property("boolean-value", true)
                        property("string-value", "sample")
                        property("input-expression", getInput("key-1"))
                        property("self-property-expression", getProperty("key-1"))
                        property("self-attribute-expression", getAttribute("key-1"))
                        property("self-artifact-expression", getArtifact("key-1"))
                        property("other-artifact-expression", getNodeTemplateArtifact("node-1", "key-1"))
                    }
                }
            }
        }

        assertNotNull(serviceTemplate.topologyTemplate, "failed to get topology template")
        assertNotNull(serviceTemplate.topologyTemplate?.nodeTemplates, "failed to get nodeTypes")
        assertNotNull(serviceTemplate.topologyTemplate?.nodeTemplates!!["activate"], "failed to get nodeTypes(activate)")
        //println(JacksonUtils.getJson(serviceTemplate, true))
    }
}
