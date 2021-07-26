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

package cba.capability.cli

import org.onap.ccsdk.cds.blueprintsprocessor.services.execution.nodeTemplateComponentScriptExecutor
import org.onap.ccsdk.cds.blueprintsprocessor.services.execution.nodeTypeComponentScriptExecutor
import org.onap.ccsdk.cds.blueprintsprocessor.ssh.relationshipTemplateSshClient
import org.onap.ccsdk.cds.blueprintsprocessor.ssh.relationshipTypeConnectsToSshClient
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintConstants
import org.onap.ccsdk.cds.controllerblueprints.core.data.ServiceTemplate
import org.onap.ccsdk.cds.controllerblueprints.core.dsl.artifactTypeTemplateVelocity
import org.onap.ccsdk.cds.controllerblueprints.core.dsl.getAttribute
import org.onap.ccsdk.cds.controllerblueprints.core.dsl.getInput
import org.onap.ccsdk.cds.controllerblueprints.core.dsl.nodeTypeComponent
import org.onap.ccsdk.cds.controllerblueprints.core.dsl.relationshipTypeConnectsTo
import org.onap.ccsdk.cds.controllerblueprints.core.dsl.serviceTemplate
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.AbstractBluePrintDefinitions

class CapabilityCliDefinitions : AbstractBluePrintDefinitions() {

    override fun serviceTemplate(): ServiceTemplate {
        return defaultServiceTemplate()
    }
}

fun CapabilityCliDefinitions.defaultServiceTemplate() =
    serviceTemplate(
        name = "capability-cli",
        version = "1.0.0",
        author = "Brinda Santh Muthuramalingam",
        tags = "brinda, tosca"
    ) {
        topologyTemplate {
            workflow(id = "check", description = "CLI Check Workflow") {
                inputs {
                    property(id = "hostname", type = "string", required = true, description = "")
                    property(id = "username", type = "string", required = true, description = "")
                    property(id = "password", type = "string", required = true, description = "")
                    property(id = "data", type = "json", required = true, description = "")
                }
                outputs {
                    property(id = "status", required = true, type = "string", description = "") {
                        value("success")
                    }
                }
                step(id = "check", target = "check", description = "Calling check script node")
            }

            /** Check component Node Template */
            nodeTemplateComponentScriptExecutor(id = "check", description = "") {
                definedOperation(description = "") {
                    inputs {
                        type(BluePrintConstants.SCRIPT_KOTLIN)
                        scriptClassReference(Check::class)
                    }
                    outputs {
                        status(getAttribute("status"))
                        responseData("""{ "data" : "Here I am "}""")
                    }
                }
                artifact(
                    id = "command-template",
                    type = "artifact-template-velocity",
                    file = "Templates/check-command-template.vtl"
                )
            }

            /** Connection Configuration through Relationship **/
            relationshipTemplateSshClient("ssh-connection-config", "Device connection config") {
                basicAuth {
                    host(getInput("hostname"))
                    password(getInput("password"))
                    username(getInput("username"))
                }
            }
        }

        /** Artifact Types */
        artifactTypeTemplateVelocity()
        /** Node Types */
        nodeTypeComponent()
        nodeTypeComponentScriptExecutor()
        /** Relationship Types */
        relationshipTypeConnectsToSshClient()
        relationshipTypeConnectsTo()
    }
