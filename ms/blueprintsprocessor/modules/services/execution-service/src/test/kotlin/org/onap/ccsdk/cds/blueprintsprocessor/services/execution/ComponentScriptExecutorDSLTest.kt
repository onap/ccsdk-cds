/*
 * Copyright © 2018-2019 AT&T Intellectual Property.
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

package org.onap.ccsdk.cds.blueprintsprocessor.services.execution

import org.junit.Test
import org.onap.ccsdk.cds.controllerblueprints.core.dsl.serviceTemplate
import kotlin.test.assertNotNull

class ComponentScriptExecutorDSLTest {

    @Test
    fun nodeTemplateComponentScriptExecutor() {

        val serviceTemplate = serviceTemplate("remote-script-dsl", "1.0.0", "xx@xx.com", "remote-script-ds") {
            topologyTemplate {
                nodeTemplateComponentScriptExecutor(
                    "script-sample",
                    "This is sample node template"
                ) {
                    definedOperation(" Sample Operation") {
                        implementation(180, "SELF")
                        inputs {
                            type("kotlin")
                            scriptClassReference("cba.sample.Processor")
                            dynamicProperties("*dynamic-inputs")
                        }
                        outputs {
                            status("success")
                        }
                    }
                }
            }
            nodeTypeComponentScriptExecutor()
        }

        // println(serviceTemplate.asJsonString(true))
        assertNotNull(serviceTemplate, "failed to service template")
        assertNotNull(serviceTemplate.nodeTypes, "failed to service template node Types")
        assertNotNull(
            serviceTemplate.nodeTypes!!["component-script-executor"],
            "failed to service template nodeType(component-script-executor)"
        )
        assertNotNull(
            serviceTemplate.topologyTemplate?.nodeTemplates?.get("script-sample"),
            "failed to nodeTemplate(script-sample)"
        )
    }
}
