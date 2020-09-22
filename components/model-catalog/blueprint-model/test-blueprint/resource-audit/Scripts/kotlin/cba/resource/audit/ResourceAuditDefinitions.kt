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

package cba.resource.audit

import org.onap.ccsdk.cds.blueprintsprocessor.services.execution.ComponentScriptExecutor
import org.onap.ccsdk.cds.blueprintsprocessor.services.execution.nodeTemplateComponentScriptExecutor
import org.onap.ccsdk.cds.blueprintsprocessor.services.execution.nodeTypeComponentScriptExecutor
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintConstants
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintTypes
import org.onap.ccsdk.cds.controllerblueprints.core.data.ServiceTemplate
import org.onap.ccsdk.cds.controllerblueprints.core.dsl.getAttribute
import org.onap.ccsdk.cds.controllerblueprints.core.dsl.getNodeTemplateAttribute
import org.onap.ccsdk.cds.controllerblueprints.core.dsl.nodeTypeComponent
import org.onap.ccsdk.cds.controllerblueprints.core.dsl.serviceTemplate
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.AbstractBluePrintDefinitions

class ResourceAuditDefinitions : AbstractBluePrintDefinitions() {

    override fun serviceTemplate(): ServiceTemplate {
        return defaultServiceTemplate()
    }
}

fun ResourceAuditDefinitions.defaultServiceTemplate() =
    serviceTemplate(
        name = "resource-audit",
        version = "1.0.0",
        author = "Brinda Santh Muthuramalingam",
        tags = "brinda, tosca"
    ) {

        topologyTemplate {

            workflow(id = "config-collect", description = "Collect the configuration for Device") {
                inputs {
                    property(id = "device-id", type = BluePrintConstants.DATA_TYPE_STRING, required = true, description = "")
                    property(id = "sources", type = BluePrintConstants.DATA_TYPE_LIST, required = true, description = "") {
                        entrySchema(BluePrintConstants.DATA_TYPE_STRING)
                    }
                }
                outputs {
                    property(id = "response-data", required = true, type = BluePrintConstants.DATA_TYPE_STRING, description = "") {
                        value(
                            getNodeTemplateAttribute(
                                nodeTemplateId = "config-collector",
                                attributeId = ComponentScriptExecutor.ATTRIBUTE_RESPONSE_DATA
                            )
                        )
                    }
                    property(id = "status", required = true, type = BluePrintConstants.DATA_TYPE_STRING, description = "") {
                        value(BluePrintConstants.STATUS_SUCCESS)
                    }
                }
                step(id = "config-collector", target = "config-collector", description = "Collect the Configuration")
            }

            val configCollectorComponent = BluePrintTypes.nodeTemplateComponentScriptExecutor(
                id = "config-collector",
                description = "Config collector component"
            ) {

                definedOperation(description = "Config Collector Operation") {
                    inputs {
                        type(BluePrintConstants.SCRIPT_KOTLIN)
                        scriptClassReference("cba.resource.audit.functions.ConfigCollector")
                    }
                    outputs {
                        status(getAttribute(ComponentScriptExecutor.ATTRIBUTE_STATUS))
                        responseData(getAttribute(ComponentScriptExecutor.ATTRIBUTE_RESPONSE_DATA))
                    }
                }
            }
            nodeTemplate(configCollectorComponent)
        }

        nodeType(BluePrintTypes.nodeTypeComponent())
        nodeType(BluePrintTypes.nodeTypeComponentScriptExecutor())
    }
