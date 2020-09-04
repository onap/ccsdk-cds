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

import com.fasterxml.jackson.databind.JsonNode
import org.junit.Test
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintTypes
import org.onap.ccsdk.cds.controllerblueprints.core.asJsonPrimitive
import org.onap.ccsdk.cds.controllerblueprints.core.data.NodeTemplate
import org.onap.ccsdk.cds.controllerblueprints.core.jsonAsJsonType
import kotlin.test.assertNotNull

class BluePrintDSLTest {

    @Test
    fun testOperationDSLWorkflow() {

        val blueprint = blueprint(
            "sample-bp", "1.0.0",
            "brindasanth@onap.com", "sample, blueprints"
        ) {

            artifactType(BluePrintTypes.artifactTypeTemplateVelocity())

            // For New Component Definition
            component(
                "resource-resolution", "component-script-executor", "1.0.0",
                "Resource Resolution component."
            ) {
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
                artifact("template1", "artifact-template-velocity", "Templates/template1.vtl")
            }

            // Already definitions Registered Components
            registryComponent(
                "activate-restconf", "component-resource-resolution", "1.0.0",
                "RestconfExecutor", "Resource Resolution component."
            ) {
                implementation(180)
                // Properties
                property("string-value1", "data")
                // Inputs
                input("json-content", """{ "name" : "cds"}""")
                // Outputs
                output("self-attribute-expression", getAttribute("template1-data"))
                // Artifacts
                artifact("template2", "artifact-template-velocity", "Templates/template1.vtl")
            }

            workflow("resource-resolution-process", "Resource Resolution wf") {
                input("json-content", "json", true, "")
                input("key-1", "string", true, "")
                output("status", "string", true, "success")
                step("resource-resolution-call", "resource-resolution", "Resource Resolution component invoke")
            }
        }
        assertNotNull(blueprint.components, "failed to get components")
        assertNotNull(blueprint.workflows, "failed to get workflows")
        // println(blueprint.asJsonString(true))

        val serviceTemplateGenerator = BluePrintServiceTemplateGenerator(blueprint)
        val serviceTemplate = serviceTemplateGenerator.serviceTemplate()
        assertNotNull(serviceTemplate.topologyTemplate, "failed to get service topology template")
        // println(serviceTemplate.asJsonString(true))
    }

    @Test
    fun testServiceTemplate() {
        val serviceTemplate = serviceTemplate(
            "sample-bp", "1.0.0",
            "brindasanth@onap.com", "sample, blueprints"
        ) {
            metadata("release", "1806")
            import("Definition/data_types.json")
            dsl("rest-endpoint", """{ "selector" : "odl-selector"}""")
            dsl("db-endpoint", """{ "selector" : "db-selector"}""")

            nodeTypeComponent()
            nodeTypeResourceSource()
            nodeTypeVnf()

            artifactTypeTemplateVelocity()
            artifactTypeTempleJinja()
            artifactTypeScriptKotlin()
            artifactTypeMappingResource()
            artifactTypeComponentJar()
            artifactTypeK8sProfileFolder()

            relationshipTypeConnectsTo()
            relationshipTypeDependsOn()
            relationshipTypeHostedOn()

            topologyTemplate {
                nodeTemplateOperation(
                    nodeTemplateName = "activate", type = "sample-node-type", interfaceName = "RestconfExecutor",
                    description = "sample activation"
                ) {
                    implementation(360, "SELF") {
                        primary("Scripts/sample.py")
                        dependencies("one", "two")
                    }
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

        // println(serviceTemplate.asJsonString(true))
        assertNotNull(serviceTemplate.artifactTypes, "failed to get artifactTypes")
        assertNotNull(serviceTemplate.nodeTypes, "failed to get nodeTypes")
        assertNotNull(serviceTemplate.relationshipTypes, "failed to get relationshipTypes")
        assertNotNull(serviceTemplate.topologyTemplate, "failed to get topology template")
        assertNotNull(serviceTemplate.topologyTemplate?.nodeTemplates, "failed to get nodeTypes")
        assertNotNull(
            serviceTemplate.topologyTemplate?.nodeTemplates!!["activate"],
            "failed to get nodeTypes(activate)"
        )
    }

    @Test
    fun testNodeTypePropertyConstrains() {
        val nodeType = nodeType("data-node", "1.0.0", "tosca.Nodes.root", "") {
            property("ip-address", "string", true, "") {
                defaultValue("127.0.0.1")
                constrain {
                    validValues(arrayListOf("""127.0.0.1""".asJsonPrimitive()))
                    length(10)
                    maxLength(20)
                    minLength(10)
                }
            }
            property("disk-space", "string", true, "") {
                defaultValue(10)
                constrain {
                    validValues("""["200KB", "400KB"]""")
                    equal("200KB")
                    inRange("""["100KB", "500KB" ]""")
                    maxLength("10MB")
                    minLength("10KB")
                }
                constrain {
                    validValues("""[ 200, 400]""")
                    greaterOrEqual("10KB")
                    greaterThan("20KB")
                    lessOrEqual("200KB")
                    lessThan("190KB")
                }
            }
        }
        assertNotNull(nodeType, "failed to get nodeType")
        // println(nodeType.asJsonString(true))
    }

    @Test
    fun testServiceTemplateWorkflow() {
        val serviceTemplate = serviceTemplate(
            "sample-bp", "1.0.0",
            "brindasanth@onap.com", "sample, blueprints"
        ) {
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
        // println(serviceTemplate.asJsonString(true))
    }

    @Test
    fun testNodeTemplateOperationTypes() {

        val testNodeTemplateInstance = BluePrintTypes.nodeTemplateComponentTestExecutor(
            id = "test-node-template",
            description = ""
        ) {
            definedProperties {
                prop1("i am property1")
                prop2("i am property2")
            }
            definedOperation("") {
                implementation(360)
                inputs {
                    request("i am request")
                }
                outputs {
                    response(getAttribute("attribute1"))
                }
            }
        }
        assertNotNull(testNodeTemplateInstance, "failed to get test node template")
        // println(testNodeTemplateInstance.asJsonString(true))
    }
}

fun BluePrintTypes.nodeTemplateComponentTestExecutor(
    id: String,
    description: String,
    block: TestNodeTemplateOperationImplBuilder.() -> Unit
):
    NodeTemplate {
    return TestNodeTemplateOperationImplBuilder(id, description).apply(block).build()
}

class TestNodeTemplateOperationImplBuilder(id: String, description: String) :
    AbstractNodeTemplateOperationImplBuilder<TestProperty, TestInput, TestOutput>(
        id, "component-test-executor",
        "ComponentTestExecutor",
        description
    )

class TestProperty : PropertiesAssignmentBuilder() {
    fun prop1(prop1: String) {
        property("prop1", prop1.asJsonPrimitive())
    }

    fun prop2(prop2: String) {
        property("prop2", prop2.asJsonPrimitive())
    }
}

class TestInput : PropertiesAssignmentBuilder() {
    fun request(request: String) {
        property("request", request.asJsonPrimitive())
    }
}

class TestOutput : PropertiesAssignmentBuilder() {
    fun response(response: String) {
        response(response.asJsonPrimitive())
    }

    fun response(response: JsonNode) {
        property("response", response)
    }
}
