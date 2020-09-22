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

package org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution

import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintTypes
import org.onap.ccsdk.cds.controllerblueprints.core.dsl.getAttribute
import kotlin.test.Test
import kotlin.test.assertNotNull

class ResourceResolutionComponentDSLTest {

    @Test
    fun testNodeTypeComponentResourceResolution() {
        val nodeType = BluePrintTypes.nodeTypeComponentResourceResolution()
        // println(nodeType.asJsonString(true))
        assertNotNull(nodeType, "failed to generate nodeTypeComponentResourceResolution")
    }

    @Test
    fun testNodeTemplateComponentResourceResolution() {
        val nodeTemplate = BluePrintTypes.nodeTemplateComponentResourceResolution("resource-resolve", "") {
            definedOperation("Resolve resources") {
                inputs {
                    actionName("resolve")
                    requestId("1234")
                    resolutionKey("vnf-1234")
                    occurrence(2)
                    resourceType("vnf")
                    storeResult(false)
                    resolutionSummary(true)
                    artifactPrefixNames(arrayListOf("template1", "template2"))
                    dynamicProperties(
                        """{
                        "prop1" : "1234",
                        "prop2" : true,
                        "prop3" : 23
                    }
                        """.trimIndent()
                    )
                }
                outputs {
                    resourceAssignmentParams(getAttribute("assignment-params"))
                    status("success")
                }
            }
        }
        // println(nodeTemplate.asJsonString(true))
        assertNotNull(nodeTemplate, "failed to generate nodeTemplateComponentResourceResolution")
    }
}
