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

package org.onap.ccsdk.cds.blueprintsprocessor.functions.python.executor

import org.junit.Test
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintTypes
import kotlin.test.assertNotNull

class ComponentRemotePythonExecutorDSLTest {

    @Test
    fun testNodeTypeComponentRemotePythonExecutor() {
        val nodeType = BluePrintTypes.nodeTypeComponentRemotePythonExecutor()
        // println(nodeType.asJsonString(true))
        assertNotNull(nodeType, "failed to generate nodeTypeComponentRemotePythonExecutor")
    }

    @Test
    fun testNodeTemplateComponentRemotePythonExecutor() {
        val nodeTemplate = BluePrintTypes.nodeTemplateComponentRemotePythonExecutor(
            "test-nodetemplate",
            "test nodetemplate"
        ) {
            definedOperation("test operation") {
                inputs {
                    endpointSelector("remote-container")
                    command("python sample.py")
                    dynamicProperties(
                        """{
                        "prop1" : "1234",
                        "prop2" : true,
                        "prop3" : 23
                    }
                        """.trimIndent()
                    )
                    argumentProperties("""["one", "two"]""")
                    packages {
                        type("pip")
                        packages(arrayListOf("ncclient", "lxml"))
                    }
                }
            }
        }
        // println(nodeTemplate.asJsonString(true))
        assertNotNull(nodeTemplate, "failed to generate nodeTemplateComponentRemotePythonExecutor")
    }
}
