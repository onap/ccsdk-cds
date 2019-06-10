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
import org.onap.ccsdk.cds.controllerblueprints.core.jsonAsJsonType
import kotlin.test.assertNotNull

class BluePrintDSLTest {

    @Test
    fun testOperationDSLWorkflow() {

        val blueprint = blueprint("sample-bp", "1.0.0",
                "brindasanth@onap.com", "sample, blueprints") {

            // For New Component Definition
            component("resource-resolution", "component-resource-resolution", "1.0.0",
                    "Resource Resolution Call") {
                implementation(180)
                // Attributes ( Properties which will be set during execution)
                attribute("template1-data", "string", true, "")

                // Properties
                property("string-value1", "string", true, "sample")
                property("string-value2", "string", true, getInput("key-1"))
                // Inputs
                input("json-content", "json", true, """{ "name" : "cds"}""")
                input("template-content", "string", true, getArtifact("template1"))
                // Outputs
                output("self-attribute-expression", "json", true, getAttribute("template1-data"))
                // Artifacts
                artifacts("template1", "artifact-velocity", "Templates/template1.vtl")
            }

            workflow("resource-resolution-process", "") {
                input("json-content", "json", true, "")
                input("key-1", "string", true, "")
                output("status", "string", true, "success")
                step("resource-resolution-call", "resource-resolution", "Resource Resolution component invoke")
            }
        }
        assertNotNull(blueprint.components, "failed to get components")
        assertNotNull(blueprint.workflows, "failed to get workflows")
        //println(blueprint.asJsonString(true))
    }

    @Test
    fun testServiceTemplate() {
        val serviceTemplate = serviceTemplate("sample-bp", "1.0.0",
                "brindasanth@onap.com", "sample, blueprints") {
            metadata("release", "1806")
            import("Definition/data_types.json")
            dsl("rest-endpoint", """{ "selector" : "odl-selector"}""")
            dsl("db-endpoint", """{ "selector" : "db-selector"}""")
            topologyTemplate {
                nodeTemplateOperation(nodeTemplateName = "activate", type = "sample-node-type", interfaceName = "RestconfExecutor",
                        description = "sample activation") {
                    inputs {
                        property("json-content", """{ "name" : "cds"}""")
                        property("array-content", """["controller", "blueprints"]""")
                        property("int-value", 234)
                        property("boolean-value", true)
                        property("string-value", "sample")
                        property("input-expression", getInput("key-1"))
                        property("self-property-expression", getProperty("key-1"))
                        property("self-artifact-expression", getArtifact("key-1"))
                        property("other-artifact-expression", getNodeTemplateArtifact("node-1", "key-1"))
                    }
                    outputs {
                        property("self-attribute-expression", getAttribute("key-1"))
                    }
                }
                // Other way of defining Node Template with artifacts, implementation
                nodeTemplate("resolve", "sample-resolve-type", "Resource Resolution") {
                    operation("ResourceResolutionExecutor", "") {
                        implementation(180)
                        inputs {
                            property("boolean-value", true)
                            property("string-value", "sample")
                        }
                        outputs {
                            property("resolve-expression", getAttribute("key-1"))
                        }
                    }
                    artifact("sample-template", "artifact-velocity", "Templates/sample-template.vtl")
                }

                workflow("resource-resolution", "to resolve resources") {
                    step("resource-resolution-call", "resolve", "Resource Resolution component invoke")
                }
                // Alternate way to define workflow
                workflow("activate", "to resolve resources") {
                    // Alternate step definition
                    step("netconf-activate-call", "activate", "call activation component") {
                        success("END")
                        failure("END")
                    }
                    inputs {
                        property("request-content", "json", true)
                    }
                    outputs {
                        property("response-content", "json", true) {
                            value(getAttribute("key-1"))
                            defaultValue("""{ "status" : "success"}""".jsonAsJsonType())
                        }
                    }
                }
            }
        }

        assertNotNull(serviceTemplate.topologyTemplate, "failed to get topology template")
        assertNotNull(serviceTemplate.topologyTemplate?.nodeTemplates, "failed to get nodeTypes")
        assertNotNull(serviceTemplate.topologyTemplate?.nodeTemplates!!["activate"], "failed to get nodeTypes(activate)")
        //println(serviceTemplate.asJsonString(true))
    }

    @Test
    fun testServiceTemplateWorkflow() {
        val serviceTemplate = serviceTemplate("sample-bp", "1.0.0",
                "brindasanth@onap.com", "sample, blueprints") {
            topologyTemplate {
                workflowNodeTemplate("activate", "component-resource-resolution", "") {
                    operation("ResourceResolutionExecutor", "") {
                        inputs {
                            property("string-value", "sample")
                        }
                    }
                }
            }
        }
        assertNotNull(serviceTemplate.topologyTemplate, "failed to get topology template")
        assertNotNull(serviceTemplate.topologyTemplate?.workflows?.get("activate"), "failed to get workflow(activate)")
        //println(serviceTemplate.asJsonString(true))
    }

}
